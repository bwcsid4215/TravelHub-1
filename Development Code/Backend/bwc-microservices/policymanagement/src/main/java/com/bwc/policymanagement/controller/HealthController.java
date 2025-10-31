package com.bwc.policymanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "API health check endpoints")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Application health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Policy Management Service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/")
    @Operation(summary = "Welcome endpoint")
    public ResponseEntity<Map<String, String>> welcome() {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome to Policy Management Service",
            "version", "1.0.0",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}