package com.glaciation.TradeOffService.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PrometheusService {
    private final Logger logger = LoggerFactory.getLogger(PrometheusService.class);
    @Autowired
    RestTemplate restTemplate;
    @Value("${prometheus.url}")
    String prometheusUrl;

    private JsonNode query(String query) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(prometheusUrl + "?query=" + query, entity, JsonNode.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Got exception {}: {}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    public JsonNode performQuery(String query) {
        JsonNode body = this.query(query);
        logger.info("Prometheus query result: {}", body);

        if (body == null) return null;

        return body.get("data").get("result").get(1);
    }
}
