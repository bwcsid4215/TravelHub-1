package com.bwc.approval_workflow_service.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionDTO {
    private UUID actionId;
    private UUID workflowId;
    private UUID travelRequestId;
    private String approverRole;
    private UUID approverId;
    private String approverName;
    private String action;
    private String step;
    private String comments;
    private String escalationReason;
    private Boolean isEscalated;
    private Double amountApproved;
    private Double reimbursementAmount;
    private LocalDateTime actionTakenAt;
    private LocalDateTime createdAt;
}