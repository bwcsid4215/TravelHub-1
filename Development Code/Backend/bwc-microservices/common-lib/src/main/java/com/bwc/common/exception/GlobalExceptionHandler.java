package com.bwc.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.bwc.common.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        String path = getRequestPath(request);
        String traceId = getTraceId();

        log.warn("Resource not found: {} - Path: {} - TraceId: {}",
                ex.getMessage(), path, traceId);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(),
                        HttpStatus.NOT_FOUND.value(), path, traceId));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, WebRequest request) {

        String path = getRequestPath(request);
        String traceId = getTraceId();

        log.warn("Business rule violation: {} - Path: {} - TraceId: {}",
                ex.getMessage(), path, traceId);

        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(),
                        ex.getStatus().value(), path, traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        String path = getRequestPath(request);
        String traceId = getTraceId();

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });

        log.warn("Validation failed: {} - Path: {} - TraceId: {}",
                errors, path, traceId);

        ApiResponse<Map<String, String>> response =
                ApiResponse.error(
                        "Validation failed. Please check the input data.",
                        HttpStatus.BAD_REQUEST.value(),
                        path,
                        traceId,
                        errors // <-- pass the map as the data payload
                );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        String path = getRequestPath(request);
        String traceId = getTraceId();

        log.error("Data integrity violation: {} - Path: {} - TraceId: {}",
                ex.getMessage(), path, traceId, ex);

        String message = "Data integrity violation. The operation cannot be completed.";
        String raw = ex.getMessage();
        if (raw != null) {
            if (raw.contains("constraint") && raw.contains("email")) {
                message = "An employee with this email already exists.";
            } else if (raw.contains("constraint") && raw.contains("unique")) {
                message = "A record with these details already exists.";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message,
                        HttpStatus.CONFLICT.value(), path, traceId));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequestExceptions(
            Exception ex, WebRequest request) {

        String path = getRequestPath(request);
        String traceId = getTraceId();

        log.warn("Bad request: {} - Path: {} - TraceId: {}",
                ex.getMessage(), path, traceId);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Malformed request. Please check your input.",
                        HttpStatus.BAD_REQUEST.value(), path, traceId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {

        String path = getRequestPath(request);
        String traceId = getTraceId();

        log.error("Unhandled exception: {} - Path: {} - TraceId: {}",
                ex.getMessage(), path, traceId, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), path, traceId));
    }

    /* ---------- Helpers ---------- */

    private String getRequestPath(WebRequest request) {
        return (request instanceof ServletWebRequest swr)
                ? swr.getRequest().getRequestURI()
                : "unknown";
    }

    private String getTraceId() {
        String id = MDC.get("traceId");
        return (id != null) ? id : UUID.randomUUID().toString();
    }
}
