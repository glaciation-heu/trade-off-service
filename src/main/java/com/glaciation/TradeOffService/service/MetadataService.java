package com.glaciation.TradeOffService.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Date;

@Service
public class MetadataService {
    private final Logger logger = LoggerFactory.getLogger(MetadataService.class);
    @Autowired
    RestTemplate restTemplate;
    @Value(value = "${metadata-service.url}")
    private String metadataServiceUrl;
    @Value(value = "${metadata-service.resource-id-template}")
    private String resourceIdTemplate;
    @Value(value = "${metadata-service.node-sparql-query}")
    private String metadataServiceNodeSparqlQuery;
    @Value(value = "${metadata-service.nodes-sparql-query}")
    private String metadataServiceNodesSparqlQuery;
    @Value(value = "${metadata-service.workload-sparql-query}")
    private String metadataServiceWorkloadSparqlQuery;
    @Value(value = "${metadata-service.workloads-sparql-query}")
    private String metadataServiceWorkloadsSparqlQuery;

    private JsonNode getQueryResult(JsonNode responseBody) {
        logger.info("Metadata service response body: {}", responseBody);
        return responseBody.get("results").get("bindings");
    }

    private JsonNode performQuery(String query) {
        logger.info("Sending query to Metadata Service: {}\n{}", metadataServiceUrl, query);
        JsonNode responseBody = restTemplate.getForEntity(metadataServiceUrl, JsonNode.class, query).getBody();
        return getQueryResult(responseBody);
    }

    public JsonNode getNodeMetadata(String nodeId, Date startDate, Date endDate) {
        String resourceId = MessageFormat.format(resourceIdTemplate, nodeId);
        String startTime = String.valueOf(startDate.getTime());
        String endTime = String.valueOf(endDate.getTime());
        String query = MessageFormat.format(metadataServiceNodeSparqlQuery, startTime, endTime, resourceId);
        return performQuery(query);
    }

    public JsonNode getNodesMetadata(Date startDate, Date endDate) {
        String startTime = String.valueOf(startDate.getTime());
        String endTime = String.valueOf(endDate.getTime());
        String query = MessageFormat.format(metadataServiceNodesSparqlQuery, startTime, endTime);
        return performQuery(query);
    }

    public JsonNode getWorkloadMetadata(String workloadId, Date startDate, Date endDate) {
        String resourceId = MessageFormat.format(resourceIdTemplate, workloadId);
        String startTime = String.valueOf(startDate.getTime());
        String endTime = String.valueOf(endDate.getTime());
        String query = MessageFormat.format(metadataServiceWorkloadSparqlQuery, resourceId, startTime, endTime);
        return performQuery(query);
    }

    public JsonNode getWorkloadsMetadata(Date startDate, Date endDate) {
        String startTime = String.valueOf(startDate.getTime());
        String endTime = String.valueOf(endDate.getTime());
        String query = MessageFormat.format(metadataServiceWorkloadsSparqlQuery, startTime, endTime);
        return performQuery(query);
    }
}
