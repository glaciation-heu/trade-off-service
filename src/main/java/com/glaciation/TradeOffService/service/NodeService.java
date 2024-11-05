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

        return ResponseEntity.ok(this.createNodePayload(nodeId, startTime, endTime, (ArrayNode) nodeMetadata, null));
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
        logger.info("Nodes metadata map size: {}", nodesMetadataMap.size());

        // query prometheus for each metric (array mode)
        // map = (nodeId, (metric, value))
        Map<String, Map<String, String>> prometheusMap = new HashMap<>();
        metricsConfiguration.getNodes().forEach(nodeMetric -> {
            nodeMetric.getMappings().stream()
                    .filter(mapping -> mapping.getPromql() != null)
                    .forEach(mapping -> {
                        String window = prometheusService.calculateDuration(startTime, endTime);
                        String truncatedEndTime = prometheusService.truncateTimestamp(endTime);
                        String query = MessageFormat.format(mapping.getPromql().getArray(), truncatedEndTime, window);
                        ArrayNode records = (ArrayNode) prometheusService.performQuery(query);
                        if (records != null) {
                            records.forEach(record -> {
                                if (record.get("metric") == null || record.get("metric").isEmpty() || record.get("metric").get("resource") == null || record.get("metric").get("resource").textValue().isEmpty()) {
                                    logger.warn("Could not find nodeId in metric record: {}", record.get("metric"));
                                    return;
                                }
                                String nodeId = record.get("metric").get("resource").textValue();
                                if (prometheusMap.containsKey(nodeId)) {
                                    prometheusMap.get(nodeId).put(nodeMetric.getName(), record.get("value").get(1).textValue());
                                } else {
                                    Map<String, String> recordsByMetric = new HashMap<>();
                                    recordsByMetric.put(nodeMetric.getName(), record.get("value").get(1).textValue());
                                    prometheusMap.put(nodeId, recordsByMetric);
                                }
                            });
                        }
                    });
        });
        logger.info("Nodes data map size: {}", prometheusMap.size());

        List<JsonNode> outputNodes = new ArrayList<>();
        nodesMetadataMap.forEach((resourceId, metadataRecords) -> {
            String resourceIdPattern = metricsConfiguration.getResourceIdPattern();
            Pattern pattern = Pattern.compile(resourceIdPattern);
            Matcher matcher = pattern.matcher(resourceId);
            if (!matcher.find()) {
                logger.error("Property 'resourceIdPattern' not matching any IDs");
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String id = matcher.group();
            Map<String, String> prometheusRecords = prometheusMap.get(id);
            outputNodes.add(this.createNodePayload(id, startTime, endTime, metadataRecords, prometheusRecords));
        });

        Map<String, Object> result = new HashMap<>();
        result.put("worker_nodes", outputNodes);

        return ResponseEntity.ok(objectMapper.valueToTree(result));
    }

    private JsonNode createNodePayload(String nodeId, Date startTime, Date endTime, ArrayNode metadataRecords, Map<String, String> prometheusMap) {
        logger.info("Creating payload for node '{}'", nodeId);
        Map<String, Object> result = new HashMap<>();
        result.put("node_id", nodeId);

        Map<String, Object> nodeResources = new HashMap<>();
        Supplier<Stream<JsonNode>> recordsStreamSupplier = () -> StreamSupport.stream(metadataRecords.spliterator(), false);

        // iterating over node mappings
        metricsConfiguration.getNodes().forEach(nodeMetric -> {
            logger.debug("Current metric: {}", nodeMetric.getName());
            Map<String, Object> resourceMap = new HashMap<>();

            nodeMetric.getMappings().forEach(mapping -> {
                if (mapping.getDkgName() == null && mapping.getPromql() == null) {
                    logger.error("Configuration error! Mapping {}.{} must have either dkgName or promql set", nodeMetric.getName(), mapping.getKey());
                    return;
//                    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                if (mapping.getDkgName() == null) {
                    // mapping is relative to prometheus data
                    if (prometheusMap == null) {
                        // perform query to prometheus
                        String window = prometheusService.calculateDuration(startTime, endTime);
                        String truncatedEndTime = prometheusService.truncateTimestamp(endTime);
                        String query = MessageFormat.format(mapping.getPromql().getSingle(), nodeId, truncatedEndTime, window);
                        JsonNode queryResult = prometheusService.performQuery(query);
                        if (queryResult != null) {
                            String value = queryResult.get(0).get("value").get(1).textValue();
                            logger.info("Setting {}.{} to value: {}", nodeMetric.getName(), mapping.getKey(), value);
                            resourceMap.put(mapping.getKey(), value);
                        }
                    } else { // array mode, already queried prometheus
                        if (!prometheusMap.containsKey(nodeMetric.getName())) return;
                        String value = prometheusMap.get(nodeMetric.getName());
                        logger.info("Setting {}.{} to value: {}", nodeMetric.getName(), mapping.getKey(), value);
                        resourceMap.put(mapping.getKey(), value);
                    }
                } else {
                    // get metadata
                    logger.debug("Looking for '{}' metric by '{}' key", mapping.getDkgName(), metricsConfiguration.getMetricNameKey());
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
//                    logger.info("Metric node: {}", metricNode);

                    String value = metricNode.get(metricsConfiguration.getMetricValueKey()).get("value").textValue();
                    logger.info("Setting {}.{} to value: {}", nodeMetric.getName(), mapping.getKey(), value);
                    resourceMap.put(mapping.getKey(), value);
                    String unit = metricNode.get(metricsConfiguration.getMetricUnitKey()).get("value").textValue();
                    resourceMap.put("unit", unit);
                }
            });
            String resourceName = nodeMetric.getName();
            nodeResources.put(resourceName, resourceMap);
        });

        result.put("resources", nodeResources);

        return objectMapper.valueToTree(result);
    }
}
