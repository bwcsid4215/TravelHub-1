package com.bwc.policymanagement.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bwc.common.dto.ApiResponse;
import com.bwc.policymanagement.dto.CityCategoryRequest;
import com.bwc.policymanagement.dto.CityCategoryResponse;
import com.bwc.policymanagement.service.CityCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "City Category Management", description = "APIs for managing city categories")
@RequiredArgsConstructor
public class CityCategoryController {

    private final CityCategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new city category")
    public ResponseEntity<ApiResponse<CityCategoryResponse>> createCategory(
            @Valid @RequestBody CityCategoryRequest request,
            HttpServletRequest httpRequest) {

        CityCategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("City category created successfully", created)
                        .path(httpRequest.getRequestURI()));
    }

    @GetMapping
    @Operation(summary = "List all city categories")
    public ResponseEntity<ApiResponse<List<CityCategoryResponse>>> getAllCategories(
            HttpServletRequest request) {

        List<CityCategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories)
                .path(request.getRequestURI()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CityCategoryResponse>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            HttpServletRequest request) {

        CityCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category)
                .path(request.getRequestURI()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CityCategoryResponse>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @Valid @RequestBody CityCategoryRequest request,
            HttpServletRequest httpRequest) {

        CityCategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("City category updated successfully", updated)
                .path(httpRequest.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            HttpServletRequest request) {

        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
            ApiResponse.<Void>success("City category deleted successfully", null)
                .path(request.getRequestURI())
        );
    }
}
