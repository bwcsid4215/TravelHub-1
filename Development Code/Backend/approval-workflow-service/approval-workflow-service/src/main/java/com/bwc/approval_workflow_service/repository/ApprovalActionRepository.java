package com.bwc.approval_workflow_service.repository;

import com.bwc.approval_workflow_service.entity.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, UUID> {
    List<ApprovalAction> findByTravelRequestIdOrderByCreatedAtDesc(UUID travelRequestId);
    List<ApprovalAction> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
}