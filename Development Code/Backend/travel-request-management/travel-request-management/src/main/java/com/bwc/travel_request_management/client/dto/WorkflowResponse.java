package com.bwc.travel_request_management.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowResponse {
    private UUID workflowId;
    private UUID travelRequestId;
    private String status;
    private String currentStage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}