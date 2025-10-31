package com.bwc.approval_workflow_service.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatsDTO {
    private String approverRole;
    private UUID approverId;
    private String approverName;
    private Long totalAssigned;
    private Long approved;
    private Long rejected;
    private Long pending;
    private Double averageProcessingTime;
}