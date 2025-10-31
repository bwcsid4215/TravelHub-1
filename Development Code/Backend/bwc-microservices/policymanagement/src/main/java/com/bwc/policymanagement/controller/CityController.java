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
import com.bwc.policymanagement.dto.CityRequest;
import com.bwc.policymanagement.dto.CityResponse;
import com.bwc.policymanagement.service.CityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cities")
@Tag(name = "City Management", description = "APIs for managing cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping
    @Operation(summary = "Create a new city under a category")
    public ResponseEntity<ApiResponse<CityResponse>> createCity(
            @Valid @RequestBody CityRequest request,
            HttpServletRequest servletRequest) {

        CityResponse city = cityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("City created successfully", city)
                        .path(servletRequest.getRequestURI()));
    }

    @GetMapping
    @Operation(summary = "List all cities")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities(
            HttpServletRequest servletRequest) {

        List<CityResponse> cities = cityService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities)
                .path(servletRequest.getRequestURI()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get city by ID")
    public ResponseEntity<ApiResponse<CityResponse>> getCityById(
            @Parameter(description = "City ID") @PathVariable UUID id,
            HttpServletRequest servletRequest) {

        CityResponse city = cityService.getCityById(id);
        return ResponseEntity.ok(ApiResponse.success(city)
                .path(servletRequest.getRequestURI()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update city")
    public ResponseEntity<ApiResponse<CityResponse>> updateCity(
            @Parameter(description = "City ID") @PathVariable UUID id,
            @Valid @RequestBody CityRequest request,
            HttpServletRequest servletRequest) {

        CityResponse city = cityService.updateCity(id, request);
        return ResponseEntity.ok(ApiResponse.success("City updated successfully", city)
                .path(servletRequest.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete city")
    public ResponseEntity<ApiResponse<Void>> deleteCity(
            @Parameter(description = "City ID") @PathVariable UUID id,
            HttpServletRequest servletRequest) {

        cityService.deleteCity(id);
        return ResponseEntity.ok(
            ApiResponse.<Void>success("City deleted successfully", null)
                .path(servletRequest.getRequestURI())
        );
    }
}
