package com.bwc.employee_management_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "APIs for monitoring application health and status")
public class HealthController {

    private final ApplicationAvailability availability;
    private final ApplicationContext context;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${info.app.version:unknown}")
    private String version;

    public HealthController(ApplicationAvailability availability, ApplicationContext context) {
        this.availability = availability;
        this.context = context;
    }

    @GetMapping
    @Operation(summary = "Application health status", 
               description = "Returns comprehensive health information about the application including version, status, and timestamps")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is healthy",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "status": "UP",
                                "application": "EmployeeManagement",
                                "version": "1.0.0",
                                "timestamp": "2023-12-07T10:30:00.000Z",
                                "liveness": "CORRECT",
                                "readiness": "ACCEPTING_TRAFFIC"
                            }
                            """))),
        @ApiResponse(responseCode = "503", description = "Application is not healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "application", applicationName,
            "version", version,
            "timestamp", Instant.now().toString(),
            "liveness", availability.getLivenessState(),
            "readiness", availability.getReadinessState(),
            "environment", System.getProperty("spring.profiles.active", "default")
        ));
    }

    @GetMapping("/liveness")
    @Operation(summary = "Liveness probe", 
               description = "Returns the liveness state of the application (primarily for Kubernetes health checks)")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(Map.of(
            "status", availability.getLivenessState().toString(),
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/readiness")
    @Operation(summary = "Readiness probe", 
               description = "Returns the readiness state of the application (primarily for Kubernetes readiness checks)")
    public ResponseEntity<Map<String, String>> readiness() {
        return ResponseEntity.ok(Map.of(
            "status", availability.getReadinessState().toString(),
            "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping("/liveness/{state}")
    @Operation(summary = "Change liveness state (Testing Only)", 
               description = "⚠️ FOR TESTING PURPOSES ONLY - Changes the liveness state for testing scenarios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liveness state changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid state provided")
    })
    public ResponseEntity<Void> changeLiveness(
            @Parameter(description = "Desired liveness state", example = "CORRECT", 
                      examples = {@ExampleObject(name = "Correct", value = "CORRECT"),
                                 @ExampleObject(name = "Broken", value = "BROKEN")})
            @PathVariable String state) {
        
        switch (state.toUpperCase()) {
            case "CORRECT" -> AvailabilityChangeEvent.publish(context, LivenessState.CORRECT);
            case "BROKEN" -> AvailabilityChangeEvent.publish(context, LivenessState.BROKEN);
            default -> throw new IllegalArgumentException("Invalid liveness state: " + state);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/readiness/{state}")
    @Operation(summary = "Change readiness state (Testing Only)", 
               description = "⚠️ FOR TESTING PURPOSES ONLY - Changes the readiness state for testing scenarios")
    public ResponseEntity<Void> changeReadiness(
            @Parameter(description = "Desired readiness state", example = "ACCEPTING_TRAFFIC")
            @PathVariable String state) {
        
        switch (state.toUpperCase()) {
            case "ACCEPTING_TRAFFIC" -> AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
            case "REFUSING_TRAFFIC" -> AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
            default -> throw new IllegalArgumentException("Invalid readiness state: " + state);
        }
        return ResponseEntity.ok().build();
    }
}