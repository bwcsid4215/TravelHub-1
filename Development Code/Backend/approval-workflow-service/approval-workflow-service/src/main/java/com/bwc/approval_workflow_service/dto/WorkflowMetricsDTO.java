package com.bwc.approval_workflow_service.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowMetricsDTO {
    private Long totalWorkflows;
    private Long pendingWorkflows;
    private Long approvedWorkflows;
    private Long rejectedWorkflows;
    private Long escalatedWorkflows;
    private Double averageApprovalTime;
}