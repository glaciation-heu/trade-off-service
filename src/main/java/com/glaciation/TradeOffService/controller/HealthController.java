package com.glaciation.TradeOffService.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/health")
public class HealthController {
    @Value("${spring.application.name}")
    String applicationName;

    @GetMapping
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(applicationName + " is up and running");
    }
}
