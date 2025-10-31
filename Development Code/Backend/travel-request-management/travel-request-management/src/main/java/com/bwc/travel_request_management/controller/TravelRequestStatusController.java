package com.bwc.travel_request_management.controller;

import com.bwc.travel_request_management.service.TravelRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/travel-requests")
@RequiredArgsConstructor
@Tag(name = "Travel Request Status", description = "Update travel request status and costs")
public class TravelRequestStatusController {

    private final TravelRequestService travelRequestService;

    @PostMapping("/{id}/status")
    @Operation(summary = "Update the travel request status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        travelRequestService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/actual-cost")
    @Operation(summary = "Update the actual cost of the travel")
    public ResponseEntity<Void> updateActualCost(
            @PathVariable UUID id,
            @RequestParam Double actualCost) {
        // Add this in your TravelRequestServiceImpl later if needed
        return ResponseEntity.ok().build();
    }
}
