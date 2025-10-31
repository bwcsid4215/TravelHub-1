package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowDTO {
    private UUID workflowId;
    private UUID travelRequestId;
    private String workflowType;
    private String currentStep;
    private String currentApproverRole;
    private UUID currentApproverId;
    private String status;
    private String previousStep;
    private String nextStep;
    private String priority;
    private Double estimatedCost;
    private Double actualCost;
    private Boolean isOverpriced;
    private String overpricedReason;
    private String comments;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
}