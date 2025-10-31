package com.bwc.approval_workflow_service.repository;

import com.bwc.approval_workflow_service.entity.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, UUID> {

    Optional<ApprovalWorkflow> findByTravelRequestId(UUID travelRequestId);

    // ✅ Add this new method
    Optional<ApprovalWorkflow> findByTravelRequestIdAndWorkflowType(UUID travelRequestId, String workflowType);

    List<ApprovalWorkflow> findByCurrentApproverRoleAndStatus(String approverRole, String status);
    List<ApprovalWorkflow> findByCurrentApproverIdAndStatus(UUID approverId, String status);
    List<ApprovalWorkflow> findByStatus(String status);
    List<ApprovalWorkflow> findByWorkflowTypeAndStatus(String workflowType, String status);

    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.currentApproverRole = :role AND w.status = 'PENDING'")
    List<ApprovalWorkflow> findPendingByApproverRole(@Param("role") String role);

    long countByStatus(String status);
    long countByCurrentApproverRoleAndStatus(String approverRole, String status);

    List<ApprovalWorkflow> findByStatusAndCurrentStep(String status, String currentStep);
    
    // Add this method for metrics
    long count();
}
