package com.glaciation.TradeOffService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.glaciation.TradeOffService.configuration.MetricsConfiguration;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class WorkloadService {
    private final Logger logger = LoggerFactory.getLogger(WorkloadService.class);
    @Autowired
    MetadataService metadataService;
    @Autowired
    PrometheusService prometheusService;
    @Autowired
    MetricsConfiguration metricsConfiguration;
    @Autowired
    ObjectMapper objectMapper;

    public ResponseEntity<Object> getWorkload(String workloadId, String startTime, String endTime) {
        // get workload metadata
        JsonNode workloadMetadata = metadataService.getWorkloadMetadata(workloadId, startTime, endTime);

        return ResponseEntity.ok(this.createWorkloadPayload(workloadId, (ArrayNode) workloadMetadata));
    }

    public ResponseEntity<JsonNode> getWorkloads(String startTime, String endTime) {
        // get workloads metadata
        ArrayNode workloadsMetadata = (ArrayNode) metadataService.getWorkloadsMetadata(startTime, endTime);

        String workloadIdKey = metricsConfiguration.getWorkloadIdKey();
        Map<String, ArrayNode> workloadsMetadataMap = new HashMap<>();
        Stream<JsonNode> workloadsMetadataStream = StreamSupport.stream(workloadsMetadata.spliterator(), false);

        // iterating over response records and invoking the mapping function
        workloadsMetadataStream.forEach(record -> {
            String workloadId = record.get(workloadIdKey).textValue();
            if (workloadsMetadataMap.containsKey(workloadId)) {
                workloadsMetadataMap.get(workloadId).add(record);
            } else {
                ArrayNode records = objectMapper.createArrayNode();
                records.add(record);
                workloadsMetadataMap.put(workloadId, records);
            }
        });

        List<JsonNode> outputWorkloads = new ArrayList<>();
        workloadsMetadataMap.forEach((id, records) -> outputWorkloads.add(this.createWorkloadPayload(id, records)));

        Map<String, Object> result = new HashMap<>();
        result.put("workloads", outputWorkloads);

        return ResponseEntity.ok(objectMapper.valueToTree(result));
    }

    private JsonNode createWorkloadPayload(String workloadId, ArrayNode metadataRecords) {
        Map<String, Object> result = new HashMap<>();
        result.put("workload_id", workloadId);

        List<Map<String, Object>> workloadResources = new ArrayList<>();
        Supplier<Stream<JsonNode>> recordsStreamSupplier = () -> StreamSupport.stream(metadataRecords.spliterator(), false);

        // iterating over workload mappings
        metricsConfiguration.getWorkloads().forEach(workloadMetric -> {
            logger.info("Current metric: {}", workloadMetric.getName());
            String resourceName = workloadMetric.getName();
            Map<String, Object> resourceMap = new HashMap<>();
            resourceMap.put("name", resourceName);

            workloadMetric.getMappings().forEach(mapping -> {
                if (mapping.getDkgName() == null && mapping.getPromql() == null) {
                    logger.error("Configuration error! Mapping {}.{} must have either dkgName or promql set", workloadMetric.getName(), mapping.getKey());
                    return;
//                    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // perform query to prometheus
                if (mapping.getDkgName() == null) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("workloadId", workloadId);
                    StringSubstitutor substitutor = new StringSubstitutor(parameters);
                    String query = substitutor.replace(mapping.getPromql());
                    JsonNode queryResult = prometheusService.performQuery(query);
                    Long value = queryResult.longValue();
                    resourceMap.put(mapping.getKey(), value);
                } else {
                    // get metadata
                    JsonNode metricNode = recordsStreamSupplier.get().filter(record -> record.get(metricsConfiguration.getMetricNameKey()).get("value").textValue().equals(mapping.getDkgName())).findFirst().orElse(null);

                    if (metricNode == null) {
                        logger.info("Metric {} not found in metadata payload", mapping.getDkgName());
                        return;
                    }

                    String value = metricNode.get(metricsConfiguration.getMetricValueKey()).get("value").textValue();
                    resourceMap.put(mapping.getKey(), value);
                    String unit = metricNode.get(metricsConfiguration.getMetricUnitKey()).get("value").textValue();
                    resourceMap.put("unit", unit);
                }
            });
            workloadResources.add(resourceMap);
        });

        result.put("resources", workloadResources);

        return objectMapper.valueToTree(result);
    }
}
