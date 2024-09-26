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
public class NodeService {
    private final Logger logger = LoggerFactory.getLogger(NodeService.class);
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

    public ResponseEntity<Object> getNode(String nodeId, Date startTime, Date endTime) {
        utils.validateDates(startTime, endTime);

        // get node metadata
        JsonNode nodeMetadata = metadataService.getNodeMetadata(nodeId, startTime, endTime);

        if (nodeMetadata == null || nodeMetadata.isEmpty()) {
            logger.warn("Response from Metadata Service is empty!");
        }

        return ResponseEntity.ok(this.createNodePayload(nodeId, startTime, endTime, (ArrayNode) nodeMetadata));
    }

    public ResponseEntity<JsonNode> getNodes(Date startTime, Date endTime) {
        utils.validateDates(startTime, endTime);

        // get nodes metadata
        ArrayNode nodesMetadata = (ArrayNode) metadataService.getNodesMetadata(startTime, endTime);

        if (nodesMetadata == null || nodesMetadata.isEmpty()) {
            logger.warn("Response from Metadata Service is empty!");
        }

        String nodeIdKey = metricsConfiguration.getNodeIdKey();
        Map<String, ArrayNode> nodesMetadataMap = new HashMap<>();
        Stream<JsonNode> nodesMetadataStream = StreamSupport.stream(nodesMetadata.spliterator(), false);

        // iterating over response records and invoking the mapping function
        nodesMetadataStream.forEach(record -> {
            if (!record.has(nodeIdKey)) {
                logger.warn("Record does not contain key: {}", nodeIdKey);
                return;
            }

            String nodeId = record.get(nodeIdKey).get("value").textValue();
            if (nodesMetadataMap.containsKey(nodeId)) {
                nodesMetadataMap.get(nodeId).add(record);
            } else {
                ArrayNode records = objectMapper.createArrayNode();
                records.add(record);
                nodesMetadataMap.put(nodeId, records);
            }
        });
        logger.info("Nodes metadata map: {}", nodesMetadataMap);

        List<JsonNode> outputNodes = new ArrayList<>();
        nodesMetadataMap.forEach((resourceId, records) -> {
            String resourceIdPattern = metricsConfiguration.getResourceIdPattern();
            Pattern pattern = Pattern.compile(resourceIdPattern);
            Matcher matcher = pattern.matcher(resourceId);
            if (!matcher.find()) {
                logger.error("Property 'resourceIdPattern' not matching any IDs");
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String id = matcher.group();
            outputNodes.add(this.createNodePayload(id, startTime, endTime, records));
        });

        Map<String, Object> result = new HashMap<>();
        result.put("worker_nodes", outputNodes);

        return ResponseEntity.ok(objectMapper.valueToTree(result));
    }

    private JsonNode createNodePayload(String nodeId, Date startTime, Date endTime, ArrayNode metadataRecords) {
        logger.info("Creating payload for node '{}'", nodeId);
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
                    String window = prometheusService.calculateDuration(startTime, endTime);
                    String truncatedEndTime = prometheusService.truncateTimestamp(endTime);
                    String query = MessageFormat.format(mapping.getPromql(), nodeId, truncatedEndTime, window);
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
            nodeResources.add(resourceMap);
        });

        result.put("resources", nodeResources);

        return objectMapper.valueToTree(result);
    }
}
