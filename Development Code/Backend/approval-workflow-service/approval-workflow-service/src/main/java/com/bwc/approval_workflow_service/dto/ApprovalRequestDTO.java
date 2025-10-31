package com.bwc.approval_workflow_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {
    @NotNull
    private UUID workflowId;
    
    @NotBlank
    private String action; // APPROVE, REJECT, RETURN, ESCALATE
    
    @NotBlank
    private String approverRole;
    
    private UUID approverId;
    private String approverName;
    private String comments;
    private String escalationReason;
    private Double amountApproved;
    private Double reimbursementAmount;
    private Boolean markOverpriced;
    private String overpricedReason;
}