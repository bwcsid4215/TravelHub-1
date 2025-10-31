package com.bwc.approval_workflow_service.service.impl;

import com.bwc.approval_workflow_service.entity.WorkflowConfiguration;
import com.bwc.approval_workflow_service.repository.WorkflowConfigurationRepository;
import com.bwc.approval_workflow_service.service.WorkflowConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowConfigurationServiceImpl implements WorkflowConfigurationService {

    private final WorkflowConfigurationRepository repository;

    @Override
    public List<WorkflowConfiguration> getActiveWorkflowByType(String workflowType) {
        log.debug("Fetching active workflow configurations for type: {}", workflowType);
        return repository.findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(workflowType);
    }

    @Override
    public List<WorkflowConfiguration> getAllByType(String workflowType) {
        log.debug("Fetching all workflow configurations for type: {}", workflowType);
        return repository.findByWorkflowTypeOrderBySequenceOrder(workflowType);
    }

    @Override
    public Optional<WorkflowConfiguration> getById(UUID configId) {
        log.debug("Fetching workflow configuration by ID: {}", configId);
        return repository.findById(configId);
    }

    @Override
    public WorkflowConfiguration save(WorkflowConfiguration configuration) {
        log.debug("Saving workflow configuration: {}", configuration.getStepName());
        // Validate sequence order is unique for this workflow type
        validateSequenceOrder(configuration);
        WorkflowConfiguration saved = repository.save(configuration);
        log.info("Workflow configuration saved successfully: {}", saved.getConfigId());
        return saved;
    }

    @Override
    public void delete(UUID configId) {
        log.debug("Deleting workflow configuration: {}", configId);
        repository.deleteById(configId);
        log.info("Workflow configuration deleted: {}", configId);
    }

    @Override
    public boolean validateWorkflowSequence(String workflowType) {
        log.debug("Validating workflow sequence for type: {}", workflowType);
        List<WorkflowConfiguration> configs = getActiveWorkflowByType(workflowType);
        
        // Check for duplicate sequence orders
        long distinctSequenceCount = configs.stream()
                .map(WorkflowConfiguration::getSequenceOrder)
                .distinct()
                .count();
        
        boolean isValid = distinctSequenceCount == configs.size();
        log.debug("Workflow sequence validation result for {}: {}", workflowType, isValid);
        return isValid;
    }

    @Override
    public void reloadConfigurations() {
        // Clear any cached configurations if caching is implemented
        log.info("Workflow configurations reloaded - cache cleared if applicable");
    }

    private void validateSequenceOrder(WorkflowConfiguration configuration) {
        List<WorkflowConfiguration> existingConfigs = getAllByType(configuration.getWorkflowType());
        
        boolean sequenceExists = existingConfigs.stream()
                .filter(c -> !c.getConfigId().equals(configuration.getConfigId())) // Exclude current config if updating
                .anyMatch(c -> c.getSequenceOrder().equals(configuration.getSequenceOrder()));
        
        if (sequenceExists) {
            String errorMsg = String.format("Sequence order %d already exists for workflow type %s", 
                    configuration.getSequenceOrder(), configuration.getWorkflowType());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        log.debug("Sequence order validation passed for workflow type: {}", configuration.getWorkflowType());
    }
}