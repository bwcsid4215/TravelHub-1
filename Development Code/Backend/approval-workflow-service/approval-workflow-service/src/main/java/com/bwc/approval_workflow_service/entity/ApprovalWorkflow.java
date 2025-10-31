package com.bwc.approval_workflow_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_workflows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID workflowId;
    
    @Column(name = "travel_request_id", nullable = false, columnDefinition = "uuid")
    private UUID travelRequestId;
    
    @Column(name = "workflow_type", nullable = false)
    @Builder.Default
    private String workflowType = "PRE_TRAVEL"; // PRE_TRAVEL or POST_TRAVEL
    
    @Column(name = "current_step", nullable = false)
    private String currentStep;
    
    @Column(name = "current_approver_role", nullable = false)
    private String currentApproverRole;
    
    @Column(name = "current_approver_id", columnDefinition = "uuid")
    private UUID currentApproverId;
    
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";
    
    @Column(name = "previous_step")
    private String previousStep;
    
    @Column(name = "next_step")
    private String nextStep;
    
    @Column(name = "priority")
    @Builder.Default
    private String priority = "NORMAL";
    
    @Column(name = "estimated_cost")
    private Double estimatedCost;
    
    @Column(name = "actual_cost")
    private Double actualCost;
    
    @Column(name = "booking_details", columnDefinition = "TEXT")
    private String bookingDetails; // JSON string of BookingDetailsDTO

    @Column(name = "total_booking_amount")
    private Double totalBookingAmount;
    
    @Column(name = "is_overpriced")
    @Builder.Default
    private Boolean isOverpriced = false;
    
    @Column(name = "overpriced_reason")
    private String overpricedReason;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Version
    private Long version;
}