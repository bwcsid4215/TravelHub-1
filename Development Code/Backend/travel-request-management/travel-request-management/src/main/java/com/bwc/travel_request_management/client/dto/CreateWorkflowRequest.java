package com.bwc.travel_request_management.client.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowRequest {
    private UUID travelRequestId;
    private UUID employeeId;
    private UUID projectId;
    private String workflowType; // "PRE" or "POST"
    private Double estimatedCost;
}
