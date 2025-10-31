package com.bwc.employee_management_service.exception;

import com.bwc.employee_management_service.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Hidden // Hide from OpenAPI documentation
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getDescription(false));
        
        ApiResponse<Void> response = ApiResponse.error(
            "Requested resource was not found: " + ex.getMessage(), 
            HttpStatus.NOT_FOUND
        );
        response.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Invalid request: {} - Path: {}", ex.getMessage(), request.getDescription(false));
        
        ApiResponse<Void> response = ApiResponse.error(
            "Invalid request: " + ex.getMessage(), 
            HttpStatus.BAD_REQUEST
        );
        response.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation error: {} - Path: {}", ex.getMessage(), request.getDescription(false));
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            "Validation failed. Please check the input data.", 
            HttpStatus.BAD_REQUEST, 
            errors
        );
        response.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.error("Data integrity violation: {} - Path: {}", ex.getMessage(), request.getDescription(false));
        
        // Extract meaningful message from the exception
        String message = "Data integrity violation. This may be due to duplicate data or invalid references.";
        if (ex.getMessage().contains("constraint") && ex.getMessage().contains("email")) {
            message = "An employee with this email already exists.";
        } else if (ex.getMessage().contains("constraint") && ex.getMessage().contains("project_name")) {
            message = "A project with this name already exists.";
        } else if (ex.getMessage().contains("constraint") && ex.getMessage().contains("role_name")) {
            message = "A role with this name already exists.";
        }
        
        ApiResponse<Void> response = ApiResponse.error(message, HttpStatus.CONFLICT);
        response.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(
            "An unexpected error occurred. Please try again later.", 
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        response.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}