package com.bwc.approval_workflow_service.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TravelRequestProxyDTO(
    UUID travelRequestId,
    UUID employeeId,
    UUID projectId,
    LocalDate startDate,
    LocalDate endDate,
    String purpose,
    Double estimatedBudget,
    String travelDestination,
    UUID managerId,
    UUID categoryId
) {}
