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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
            logger.info("Sending query to Prometheus: {}\n{}", prometheusUrl, query);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(prometheusUrl, entity, JsonNode.class, query);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Got exception {}: {}", e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    public JsonNode performQuery(String query) {
        JsonNode body = this.query(query);
        String responseBodyString = body.toString();
        String responseBodyLog = responseBodyString.length() > 1000 ? responseBodyString.substring(0, 1000) + "..." : responseBodyString;
        logger.info("Prometheus query result: {}", responseBodyLog);

        if (body == null) return null;

        if (body.get("data").get("result").isEmpty()) return null;

        return body.get("data").get("result");
    }

    public String truncateTimestamp(Date endTime) {
        String endTimeString = String.valueOf(endTime.getTime());
        return endTimeString.substring(0, 10);
    }

    public String calculateDuration(Date startDate, Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();

        LocalDateTime start = Instant.ofEpochMilli(startTime)
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();
        LocalDateTime end = Instant.ofEpochMilli(endTime)
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();

        Duration duration = Duration.between(start, end);
        long durationInMs = duration.toMillis();

        if (durationInMs < 1000) return durationInMs + "ms";
        else if (durationInMs < 1000 * 60) {
            long durationInSeconds = (durationInMs / 1000) % 60;
            return durationInSeconds + "s";
        } else if (durationInMs < 1000 * 60 * 60) {
            long durationInMinutes = (durationInMs / (1000 * 60)) % 60;
            return durationInMinutes + "m";
        } else if (durationInMs < 1000 * 60 * 60 * 24) {
            long durationInHours = (durationInMs / (1000 * 60 * 60)) % 24;
            return durationInHours + "h";
        } else if (durationInMs < 1000 * 60 * 60 * 24 * 7) {
            long durationInDays = (durationInMs / (1000 * 60 * 60 * 24)) % 7;
            return durationInDays + "d";
        } else if (durationInMs < 1000L * 60 * 60 * 24 * 7 * 52) {
            long durationInWeeks = (durationInMs / (1000 * 60 * 60 * 24 * 7)) % 52;
            return durationInWeeks + "w";
        } else {
            long durationInYears = durationInMs / (1000L * 60 * 60 * 24 * 365);
            return durationInYears + "y";
        }
    }
}
