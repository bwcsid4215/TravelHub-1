package com.bwc.approval_workflow_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalAction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID actionId;
    
    @Column(name = "workflow_id", nullable = false, columnDefinition = "uuid")
    private UUID workflowId;
    
    @Column(name = "travel_request_id", nullable = false, columnDefinition = "uuid")
    private UUID travelRequestId;
    
    @Column(name = "approver_role", nullable = false)
    private String approverRole;
    
    @Column(name = "approver_id", columnDefinition = "uuid")
    private UUID approverId;
    
    @Column(name = "approver_name")
    private String approverName;
    
    @Column(name = "action", nullable = false, length = 20)
    private String action; // APPROVE, REJECT, RETURN, ESCALATE
    
    @Column(name = "step", nullable = false)
    private String step;
    
    @Column(name = "comments", length = 1000)
    private String comments;
    
    @Column(name = "action_taken_at")
    private LocalDateTime actionTakenAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "escalation_reason")
    private String escalationReason;
    
    @Column(name = "is_escalated")
    @Builder.Default
    private Boolean isEscalated = false;
    
    @Column(name = "amount_approved")
    private Double amountApproved;
    
    @Column(name = "reimbursement_amount")
    private Double reimbursementAmount;
}