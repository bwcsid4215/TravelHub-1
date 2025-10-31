package com.bwc.travel_request_management.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bwc.travel_request_management.dto.TravelRequestDTO;
import com.bwc.travel_request_management.dto.TravelRequestProxyDTO;
import com.bwc.travel_request_management.service.TravelRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travel-requests")
@RequiredArgsConstructor
@Validated
@Tag(name = "Travel Requests", description = "CRUD operations for managing travel requests")
public class TravelRequestController {

    private final TravelRequestService service;

    @Operation(summary = "Create a new travel request")
    @PostMapping
    public ResponseEntity<TravelRequestDTO> create(@Valid @RequestBody TravelRequestDTO dto) {
        return ResponseEntity.ok(service.createRequest(dto));
    }

    @Operation(summary = "Get a travel request by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TravelRequestDTO> get(@Parameter(description = "Travel Request ID") @PathVariable UUID id) {
        return ResponseEntity.ok(service.getRequest(id));
    }

   
    @Operation(summary = "List all travel requests")
    @GetMapping
    public ResponseEntity<List<TravelRequestDTO>> getAll() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @Operation(summary = "Get paginated travel requests")
    @GetMapping("/page")
    public ResponseEntity<Page<TravelRequestDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAllRequests(pageable));
    }

    @Operation(summary = "Update travel request")
    @PutMapping("/{id}")
    public ResponseEntity<TravelRequestDTO> update(@PathVariable UUID id, @Valid @RequestBody TravelRequestDTO dto) {
        return ResponseEntity.ok(service.updateRequest(id, dto));
    }

    @Operation(summary = "Patch travel request (partial update)")
    @PatchMapping("/{id}")
    public ResponseEntity<TravelRequestDTO> patch(@PathVariable UUID id, @RequestBody TravelRequestDTO dto) {
        return ResponseEntity.ok(service.patchRequest(id, dto));
    }

    @Operation(summary = "Delete travel request")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Update travel request status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update actual cost")
    @PatchMapping("/{id}/actual-cost")
    public ResponseEntity<Void> updateActualCost(@PathVariable UUID id, @RequestParam Double actualCost) {
        // You'll need to add this method to your service
        // service.updateActualCost(id, actualCost);
        return ResponseEntity.ok().build();
    }
}