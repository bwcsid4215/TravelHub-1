package com.bwc.approval_workflow_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "workflow_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID configId;
    
    @Column(name = "workflow_type", nullable = false)
    private String workflowType; // PRE_TRAVEL or POST_TRAVEL
    
    @Column(name = "step_name", nullable = false)
    private String stepName;
    
    @Column(name = "approver_role", nullable = false)
    private String approverRole;
    
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;
    
    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;
    
    @Column(name = "time_limit_hours")
    private Integer timeLimitHours;
    
    @Column(name = "auto_approve_after_timeout")
    @Builder.Default
    private Boolean autoApproveAfterTimeout = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}