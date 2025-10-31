package com.bwc.employee_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "Standard API response wrapper for all endpoints")
@Data
public class ApiResponse<T> {
    
    @Schema(description = "Indicates whether the request was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message describing the result", example = "Operation completed successfully")
    private String message;
    
    @Schema(description = "The actual data payload of the response")
    private T data;
    
    @Schema(description = "HTTP status code", example = "200")
    private int status;
    
    @Schema(description = "Timestamp when the response was generated", example = "2023-12-07T10:30:00.000Z")
    private String timestamp;
    
    @Schema(description = "API endpoint path that was called", example = "/api/v1/employees")
    private String path;

    @Schema(description = "Unique identifier for tracing the request", example = "req-123456")
    private String requestId;

    public ApiResponse(boolean success, String message, T data, HttpStatus status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status.value();
        this.timestamp = LocalDateTime.now().toString();
        this.requestId = generateRequestId();
    }

    public ApiResponse(boolean success, String message, T data, HttpStatus status, String path) {
        this(success, message, data, status);
        this.path = path;
    }

    // Static factory methods
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, message, data, HttpStatus.CREATED);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(false, message, null, status);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status, T errors) {
        return new ApiResponse<>(false, message, errors, status);
    }

    public static <T> ApiResponse<T> success(T data, String message, String path) {
        return new ApiResponse<>(true, message, data, HttpStatus.OK, path);
    }

    private String generateRequestId() {
        return "req-" + System.currentTimeMillis() + "-" + Math.abs(hashCode());
    }
}