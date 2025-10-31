package com.bwc.travel_request_management.controller;

import com.bwc.travel_request_management.client.EmployeeServiceClient;
import com.bwc.travel_request_management.dto.TravelRequestProxyDTO;
import com.bwc.travel_request_management.entity.TravelRequest;
import com.bwc.travel_request_management.repository.TravelRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/travel-requests/proxy")
@Tag(name = "Travel Request Proxy", description = "Lightweight read-only travel request representation")
@RequiredArgsConstructor
public class TravelRequestProxyController {

    private final TravelRequestRepository repository;
    private final EmployeeServiceClient employeeClient;

    @Operation(summary = "Get lightweight travel request with manager info")
    @GetMapping("/{id}")
    public TravelRequestProxyDTO getById(
            @Parameter(description = "Travel Request ID") @PathVariable UUID id) {

        TravelRequest req = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel request not found"));

        UUID managerId = null;
        try {
            var employee = employeeClient.getEmployee(req.getEmployeeId());
            managerId = employee.getManagerId();
            log.info("✅ Manager fetched for employee {} -> {}", req.getEmployeeId(), managerId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to fetch manager for employee {}: {}", req.getEmployeeId(), e.getMessage());
        }

        return TravelRequestProxyDTO.builder()
                .travelRequestId(req.getTravelRequestId())
                .employeeId(req.getEmployeeId())
                .projectId(req.getProjectId())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .purpose(req.getPurpose())
                .estimatedBudget(req.getEstimatedBudget())
                .travelDestination(req.getTravelDestination())
                .origin(req.getOrigin())
                .managerId(managerId)
                .build();
    }
}