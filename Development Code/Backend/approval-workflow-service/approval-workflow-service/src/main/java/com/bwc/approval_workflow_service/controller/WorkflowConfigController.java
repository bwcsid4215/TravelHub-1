package com.bwc.approval_workflow_service.controller;

import com.bwc.approval_workflow_service.entity.WorkflowConfiguration;
import com.bwc.approval_workflow_service.service.WorkflowConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow-configs")
@RequiredArgsConstructor
public class WorkflowConfigController {

    private final WorkflowConfigurationService workflowConfigurationService;

    @PostMapping
    public ResponseEntity<WorkflowConfiguration> createConfig(@RequestBody WorkflowConfigRequest request) {
        WorkflowConfiguration config = WorkflowConfiguration.builder()
                .workflowType(request.workflowType())
                .stepName(request.stepName())
                .approverRole(request.approverRole())
                .sequenceOrder(request.sequenceOrder())
                .isMandatory(request.isMandatory())
                .timeLimitHours(request.timeLimitHours())
                .autoApproveAfterTimeout(request.autoApproveAfterTimeout())
                .isActive(request.isActive())
                .build();
        
        WorkflowConfiguration saved = workflowConfigurationService.save(config);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<WorkflowConfiguration>> getAllConfigs() {
        // This would need a new method in service to get all configs
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{workflowType}")
    public ResponseEntity<List<WorkflowConfiguration>> getConfigs(@PathVariable String workflowType) {
        List<WorkflowConfiguration> configs = workflowConfigurationService.getAllByType(workflowType);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{workflowType}/active")
    public ResponseEntity<List<WorkflowConfiguration>> getActiveConfigs(@PathVariable String workflowType) {
        List<WorkflowConfiguration> configs = workflowConfigurationService.getActiveWorkflowByType(workflowType);
        return ResponseEntity.ok(configs);
    }

    @PutMapping("/{configId}")
    public ResponseEntity<WorkflowConfiguration> updateConfig(
            @PathVariable UUID configId, 
            @RequestBody WorkflowConfigRequest request) {
        
        WorkflowConfiguration existing = workflowConfigurationService.getById(configId)
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
        
        // Update fields
        existing.setWorkflowType(request.workflowType());
        existing.setStepName(request.stepName());
        existing.setApproverRole(request.approverRole());
        existing.setSequenceOrder(request.sequenceOrder());
        existing.setIsMandatory(request.isMandatory());
        existing.setTimeLimitHours(request.timeLimitHours());
        existing.setAutoApproveAfterTimeout(request.autoApproveAfterTimeout());
        existing.setIsActive(request.isActive());
        
        WorkflowConfiguration updated = workflowConfigurationService.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{configId}")
    public ResponseEntity<Void> deleteConfig(@PathVariable UUID configId) {
        workflowConfigurationService.delete(configId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reload")
    public ResponseEntity<Void> reloadConfigs() {
        workflowConfigurationService.reloadConfigurations();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{workflowType}/validate")
    public ResponseEntity<Boolean> validateSequence(@PathVariable String workflowType) {
        boolean isValid = workflowConfigurationService.validateWorkflowSequence(workflowType);
        return ResponseEntity.ok(isValid);
    }

    public record WorkflowConfigRequest(
        String workflowType,
        String stepName,
        String approverRole,
        Integer sequenceOrder,
        Boolean isMandatory,
        Integer timeLimitHours,
        Boolean autoApproveAfterTimeout,
        Boolean isActive
    ) {}
}