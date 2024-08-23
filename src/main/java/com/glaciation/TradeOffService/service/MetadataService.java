package com.glaciation.TradeOffService.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;

@Service
public class MetadataService {
    @Autowired
    RestTemplate restTemplate;
    @Value(value = "${metadata-service.url}")
    private String metadataServiceUrl;
    @Value(value = "${metadata-service.node-sparql-query}")
    private String metadataServiceNodeSparqlQuery;
    @Value(value = "${metadata-service.nodes-sparql-query}")
    private String metadataServiceNodesSparqlQuery;
    @Value(value = "${metadata-service.workload-sparql-query}")
    private String metadataServiceWorkloadSparqlQuery;
    @Value(value = "${metadata-service.workloads-sparql-query}")
    private String metadataServiceWorkloadsSparqlQuery;

    private JsonNode getQueryResult(JsonNode responseBody) {
        return responseBody.get("results").get("bindings");
    }

    public JsonNode getNodeMetadata(String nodeId, String startTime, String endTime) {
        String query = MessageFormat.format(metadataServiceNodeSparqlQuery, nodeId, startTime, endTime);
        String url = metadataServiceUrl + "?SPARQLquery=" + query;
        JsonNode responseBody = restTemplate.getForEntity(url, JsonNode.class).getBody();
        return getQueryResult(responseBody);
    }

    public JsonNode getNodesMetadata(String startTime, String endTime) {
        String query = MessageFormat.format(metadataServiceNodesSparqlQuery, startTime, endTime);
        String url = metadataServiceUrl + "?SPARQLquery=" + query;
        JsonNode responseBody = restTemplate.getForEntity(url, JsonNode.class).getBody();
        return getQueryResult(responseBody);
    }

    public JsonNode getWorkloadMetadata(String workloadId, String startTime, String endTime) {
        String query = MessageFormat.format(metadataServiceWorkloadSparqlQuery, workloadId, startTime, endTime);
        String url = metadataServiceUrl + "?SPARQLquery=" + query;
        JsonNode responseBody = restTemplate.getForEntity(url, JsonNode.class).getBody();
        return getQueryResult(responseBody);
    }

    public JsonNode getWorkloadsMetadata(String startTime, String endTime) {
        String query = MessageFormat.format(metadataServiceWorkloadsSparqlQuery, startTime, endTime);
        String url = metadataServiceUrl + "?SPARQLquery=" + query;
        JsonNode responseBody = restTemplate.getForEntity(url, JsonNode.class).getBody();
        return getQueryResult(responseBody);
    }
}
