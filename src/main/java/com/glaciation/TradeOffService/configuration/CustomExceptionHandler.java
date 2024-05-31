package com.glaciation.TradeOffService.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    @Autowired
    ObjectMapper mapper;

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<Object> handleHttpException(HttpClientErrorException ex, WebRequest request) {
        Map<String, Object> map = new HashMap<>();

        String message;
        try {
            JsonNode error = mapper.readTree(ex.getResponseBodyAsString());
            message = error.get("message").asText();
        } catch (JsonProcessingException e) {
            message = ex.getResponseBodyAsString();
        }

        map.put("message", message);
        map.put("status", ex.getStatusText());
        map.put("statusCode", ex.getStatusCode().value());
        JsonNode body = mapper.valueToTree(map);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(value = {MissingServletRequestParameterException.class, MissingRequestHeaderException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleClientException(Exception ex, WebRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", ex.getMessage());
        map.put("status", HttpStatus.BAD_REQUEST);
        map.put("statusCode", HttpStatus.BAD_REQUEST.value());
        JsonNode body = mapper.valueToTree(map);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
