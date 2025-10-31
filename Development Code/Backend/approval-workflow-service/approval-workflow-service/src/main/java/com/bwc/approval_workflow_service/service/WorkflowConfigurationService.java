package com.bwc.approval_workflow_service.service;

import com.bwc.approval_workflow_service.entity.WorkflowConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowConfigurationService {
    
    // Get active configurations for a workflow type
    List<WorkflowConfiguration> getActiveWorkflowByType(String workflowType);
    
    // Get all configurations for a workflow type (active and inactive)
    List<WorkflowConfiguration> getAllByType(String workflowType);
    
    // Get configuration by ID
    Optional<WorkflowConfiguration> getById(UUID configId);
    
    // Save or update configuration
    WorkflowConfiguration save(WorkflowConfiguration configuration);
    
    // Delete configuration
    void delete(UUID configId);
    
    // Validate workflow configuration sequence
    boolean validateWorkflowSequence(String workflowType);
    
    // Reload configurations (clear cache, etc.)
    void reloadConfigurations();
}