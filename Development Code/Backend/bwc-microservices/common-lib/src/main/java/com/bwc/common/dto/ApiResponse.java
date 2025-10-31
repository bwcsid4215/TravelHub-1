package com.bwc.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message describing the result")
    private String message;

    @Schema(description = "The actual data payload of the response")
    private T data;

    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Schema(description = "Timestamp when the response was generated", example = "2025-09-25T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "API endpoint path that was called", example = "/api/v1/employees")
    private String path;

    @Schema(description = "Unique identifier for tracing the request")
    private String traceId;

    @Schema(description = "Additional metadata (pagination, filters, etc.)")
    private Map<String, Object> metadata;

    // Private constructor
    private ApiResponse(boolean success, String message, T data, int status, String path, String traceId) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status;
        this.path = path;
        this.traceId = traceId != null ? traceId : UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    // Static factory methods with consistent signatures
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, 200, null, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 200, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message, String path, String traceId) {
        return new ApiResponse<>(true, message, data, 200, path, traceId);
    }

    public static <T> ApiResponse<T> created(T data, String message, String path, String traceId) {
        return new ApiResponse<>(true, message, data, 201, path, traceId);
    }

    public static <T> ApiResponse<T> error(String message, int status, String path, String traceId) {
        return new ApiResponse<>(false, message, null, status, path, traceId);
    }

    public static <T> ApiResponse<T> error(String message, int status, String path, String traceId, T data) {
        return new ApiResponse<>(false, message, data, status, path, traceId);
    }

    // Fluent builder methods
    public ApiResponse<T> path(String path) {
        this.path = path;
        return this;
    }

    public ApiResponse<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ApiResponse<T> withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
}