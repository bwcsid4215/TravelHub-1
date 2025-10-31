package com.bwc.approval_workflow_service.repository;

import com.bwc.approval_workflow_service.entity.WorkflowConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowConfigurationRepository extends JpaRepository<WorkflowConfiguration, UUID> {
    
    // Get active configurations for a workflow type, ordered by sequence
    List<WorkflowConfiguration> findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(String workflowType);
    
    // Get all configurations for a workflow type (active and inactive), ordered by sequence
    List<WorkflowConfiguration> findByWorkflowTypeOrderBySequenceOrder(String workflowType);
    
    // Additional useful methods
    List<WorkflowConfiguration> findByApproverRoleAndIsActiveTrue(String approverRole);
    List<WorkflowConfiguration> findByIsActiveTrue();
}