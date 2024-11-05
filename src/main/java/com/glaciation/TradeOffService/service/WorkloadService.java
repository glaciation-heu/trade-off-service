package com.glaciation.TradeOffService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.glaciation.TradeOffService.configuration.MetricsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    Utils utils;
    @Autowired
    ObjectMapper objectMapper;

    public ResponseEntity<Object> getWorkload(String workloadId, Date startTime, Date endTime) {
        utils.validateDates(startTime, endTime);

        // get workload metadata
        JsonNode workloadMetadata = metadataService.getWorkloadMetadata(workloadId, startTime, endTime);

        if (workloadMetadata == null || workloadMetadata.isEmpty()) {
            logger.warn("Response from Metadata Service is empty!");
        }

        return ResponseEntity.ok(this.createWorkloadPayload(workloadId, startTime, endTime, (ArrayNode) workloadMetadata));
    }

    public ResponseEntity<Object> getWorkloads(Date startTime, Date endTime) {
        utils.validateDates(startTime, endTime);

        // get workloads metadata
        ArrayNode workloadsMetadata = (ArrayNode) metadataService.getWorkloadsMetadata(startTime, endTime);

        if (workloadsMetadata == null || workloadsMetadata.isEmpty()) {
            logger.warn("Response from Metadata Service is empty!");
        }

        String workloadIdKey = metricsConfiguration.getWorkloadIdKey();
        Map<String, ArrayNode> workloadsMetadataMap = new HashMap<>();
        Stream<JsonNode> workloadsMetadataStream = StreamSupport.stream(workloadsMetadata.spliterator(), false);

        // iterating over response records and invoking the mapping function
        workloadsMetadataStream.forEach(record -> {
            if (!record.has(workloadIdKey)) {
                logger.warn("Record does not contain key: {}", workloadIdKey);
                return;
            }

            String workloadId = record.get(workloadIdKey).get("value").textValue();
            if (workloadsMetadataMap.containsKey(workloadId)) {
                workloadsMetadataMap.get(workloadId).add(record);
            } else {
                ArrayNode records = objectMapper.createArrayNode();
                records.add(record);
                workloadsMetadataMap.put(workloadId, records);
            }
        });
        logger.info("Workloads metadata map: {}", workloadsMetadataMap);

        List<JsonNode> outputWorkloads = new ArrayList<>();
        workloadsMetadataMap.forEach((resourceId, records) -> {
            String resourceIdPattern = metricsConfiguration.getResourceIdPattern();
            Pattern pattern = Pattern.compile(resourceIdPattern);
            Matcher matcher = pattern.matcher(resourceId);
            if (!matcher.find()) {
                logger.error("Property 'resourceIdPattern' not matching any IDs");
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String id = matcher.group();
            outputWorkloads.add(this.createWorkloadPayload(id, startTime, endTime, records));
        });

        Map<String, Object> result = new HashMap<>();
        result.put("workloads", outputWorkloads);

        return ResponseEntity.ok(objectMapper.valueToTree(result));
    }

    private JsonNode createWorkloadPayload(String workloadId, Date startTime, Date endTime, ArrayNode metadataRecords) {
        logger.info("Creating payload for workload '{}'", workloadId);
        Map<String, Object> result = new HashMap<>();
        result.put("workload_id", workloadId);

        Map<String, Object> workloadResources = new HashMap<>();
        Supplier<Stream<JsonNode>> recordsStreamSupplier = () -> StreamSupport.stream(metadataRecords.spliterator(), false);

        // iterating over workload mappings
        metricsConfiguration.getWorkloads().forEach(workloadMetric -> {
            logger.debug("Current metric: {}", workloadMetric.getName());
            Map<String, Object> resourceMap = new HashMap<>();

            workloadMetric.getMappings().forEach(mapping -> {
                if (mapping.getDkgName() == null && mapping.getPromql() == null) {
                    logger.error("Configuration error! Mapping {}.{} must have either dkgName or promql set", workloadMetric.getName(), mapping.getKey());
                    return;
//                    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // perform query to prometheus
                if (mapping.getDkgName() == null) {
                    String window = prometheusService.calculateDuration(startTime, endTime);
                    String truncatedEndTime = prometheusService.truncateTimestamp(endTime);
                    String podId = workloadId.split("\\.")[1]; // remove namespace from workload id
                    String query = MessageFormat.format(mapping.getPromql(), podId, truncatedEndTime, window);
                    JsonNode queryResult = prometheusService.performQuery(query);
                    if (queryResult != null) {
                        String value = queryResult.textValue();
                        logger.info("Setting {} to value: {}", mapping.getKey(), value);
                        resourceMap.put(mapping.getKey(), value);
                    }
                } else {
                    // get metadata
                    logger.info("Looking for '{}' metric by '{}' key", mapping.getDkgName(), metricsConfiguration.getMetricNameKey());
                    JsonNode metricNode = recordsStreamSupplier.get().filter(record -> {
                        JsonNode matchingNode = record.get(metricsConfiguration.getMetricNameKey());
                        if (matchingNode == null) {
                            logger.warn("Record has no '{}' key", metricsConfiguration.getMetricNameKey());
                            return false;
                        }
                        return matchingNode.get("value").textValue().equals(mapping.getDkgName());
                    }).findFirst().orElse(null);

                    if (metricNode == null) {
                        logger.info("Metric '{}' not found in metadata payload", mapping.getDkgName());
                        return;
                    }
                    logger.info("Metric node: {}", metricNode);

                    String value = metricNode.get(metricsConfiguration.getMetricValueKey()).get("value").textValue();
                    resourceMap.put(mapping.getKey(), value);
                    String unit = metricNode.get(metricsConfiguration.getMetricUnitKey()).get("value").textValue();
                    resourceMap.put("unit", unit);
                }
            });
            String resourceName = workloadMetric.getName();
            workloadResources.put(resourceName, resourceMap);
        });

        result.put("resources", workloadResources);

        Map<String, Object> runsOnMap = new HashMap<>();
        if (!metadataRecords.isEmpty()) {
            runsOnMap.put("worker_node_id", metadataRecords.get(0).get(metricsConfiguration.getWorkloadNodeKey()).get("value").textValue());
            runsOnMap.put("start_time", metadataRecords.get(0).get(metricsConfiguration.getWorkloadStartTimeKey()).get("value").textValue());
            if (metadataRecords.get(0).has(metricsConfiguration.getWorkloadEndTimeKey())) {
                runsOnMap.put("end_time", metadataRecords.get(0).get(metricsConfiguration.getWorkloadEndTimeKey()).get("value").textValue());
            }
        }
        result.put("runs_on", runsOnMap);

        return objectMapper.valueToTree(result);
    }
}
