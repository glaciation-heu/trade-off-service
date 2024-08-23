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
public class NodeService {
    private final Logger logger = LoggerFactory.getLogger(NodeService.class);
    @Autowired
    MetadataService metadataService;
    @Autowired
    PrometheusService prometheusService;
    @Autowired
    MetricsConfiguration metricsConfiguration;
    @Autowired
    ObjectMapper objectMapper;

    public ResponseEntity<Object> getNode(String nodeId, String startTime, String endTime) {
        // get node metadata
        JsonNode nodeMetadata = metadataService.getNodeMetadata(nodeId, startTime, endTime);

        return ResponseEntity.ok(this.createNodePayload(nodeId, (ArrayNode) nodeMetadata));
    }

    public ResponseEntity<JsonNode> getNodes(String startTime, String endTime) {
        // get nodes metadata
        ArrayNode nodesMetadata = (ArrayNode) metadataService.getNodesMetadata(startTime, endTime);

        String nodeIdKey = metricsConfiguration.getNodeIdKey();
        Map<String, ArrayNode> nodesMetadataMap = new HashMap<>();
        Stream<JsonNode> nodesMetadataStream = StreamSupport.stream(nodesMetadata.spliterator(), false);

        // iterating over response records and invoking the mapping function
        nodesMetadataStream.forEach(record -> {
            String nodeId = record.get(nodeIdKey).textValue();
            if (nodesMetadataMap.containsKey(nodeId)) {
                nodesMetadataMap.get(nodeId).add(record);
            } else {
                ArrayNode records = objectMapper.createArrayNode();
                records.add(record);
                nodesMetadataMap.put(nodeId, records);
            }
        });

        List<JsonNode> outputNodes = new ArrayList<>();
        nodesMetadataMap.forEach((id, records) -> outputNodes.add(this.createNodePayload(id, records)));

        Map<String, Object> result = new HashMap<>();
        result.put("worker_nodes", outputNodes);

        return ResponseEntity.ok(objectMapper.valueToTree(result));
    }

    private JsonNode createNodePayload(String nodeId, ArrayNode metadataRecords) {
        Map<String, Object> result = new HashMap<>();
        result.put("node_id", nodeId);

        List<Map<String, Object>> nodeResources = new ArrayList<>();
        Supplier<Stream<JsonNode>> recordsStreamSupplier = () -> StreamSupport.stream(metadataRecords.spliterator(), false);

        // iterating over node mappings
        metricsConfiguration.getNodes().forEach(nodeMetric -> {
            logger.info("Current metric: {}", nodeMetric.getName());
            String resourceName = nodeMetric.getName();
            Map<String, Object> resourceMap = new HashMap<>();
            resourceMap.put("name", resourceName);

            nodeMetric.getMappings().forEach(mapping -> {
                if (mapping.getDkgName() == null && mapping.getPromql() == null) {
                    logger.error("Configuration error! Mapping {}.{} must have either dkgName or promql set", nodeMetric.getName(), mapping.getKey());
                    return;
//                    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // perform query to prometheus
                if (mapping.getDkgName() == null) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("nodeId", nodeId);
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
            nodeResources.add(resourceMap);
        });

        result.put("resources", nodeResources);

        return objectMapper.valueToTree(result);
    }
}
