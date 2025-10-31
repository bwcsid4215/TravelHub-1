package com.bwc.approval_workflow_service.controller;

import com.bwc.approval_workflow_service.dto.WorkflowMetricsDTO;
import com.bwc.approval_workflow_service.service.ApprovalWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/workflows")
@RequiredArgsConstructor
@Tag(name = "Admin Workflows", description = "Admin operations for workflow monitoring and configuration")
public class AdminWorkflowController {

    private final ApprovalWorkflowService workflowService;

    @Operation(summary = "Get workflow metrics", description = "Fetch aggregated workflow statistics")
    @GetMapping("/metrics")
    public ResponseEntity<WorkflowMetricsDTO> getWorkflowMetrics() {
        return ResponseEntity.ok(workflowService.getWorkflowMetrics());
    }

    @Operation(summary = "Get overdue workflows", description = "List workflows that are overdue for approval")
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueWorkflows() {
        return ResponseEntity.ok(workflowService.getWorkflowsByStatus("OVERDUE"));
    }

    @Operation(summary = "Reload workflow configurations", description = "Force reload workflow configuration from database")
    @PostMapping("/reload-config")
    public ResponseEntity<Void> reloadWorkflowConfigurations() {
        workflowService.reloadWorkflowConfigurations();
        return ResponseEntity.ok().build();
    }
}
