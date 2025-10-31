package com.bwc.approval_workflow_service.controller;

import com.bwc.approval_workflow_service.dto.ApprovalRequestDTO;
import com.bwc.approval_workflow_service.dto.ApprovalWorkflowDTO;
import com.bwc.approval_workflow_service.service.ApprovalWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance/approvals")
@RequiredArgsConstructor
@Tag(name = "Finance Approvals", description = "Finance team approval workflows")
public class FinanceApprovalController {

    private final ApprovalWorkflowService workflowService;

    @Operation(summary = "Get pending approvals for Finance")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<List<ApprovalWorkflowDTO>> getPendingApprovals() {
        return ResponseEntity.ok(workflowService.getPendingApprovalsByRole("FINANCE"));
    }

    @Operation(summary = "Process Finance approval")
    @PostMapping("/{workflowId}/action")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<ApprovalWorkflowDTO> takeFinanceAction(
            @PathVariable UUID workflowId,
            @RequestBody ApprovalRequestDTO approvalRequest,
            HttpServletRequest request) {
        
        String financeIdHeader = request.getHeader("X-User-Id");
        UUID financeId = financeIdHeader != null ? UUID.fromString(financeIdHeader) : null;
        
        approvalRequest.setWorkflowId(workflowId);
        approvalRequest.setApproverRole("FINANCE");
        approvalRequest.setApproverId(financeId);
        
        return ResponseEntity.ok(workflowService.processApproval(approvalRequest));
    }

    @Operation(summary = "Get Finance approval statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<?> getApprovalStats(HttpServletRequest request) {
        String financeIdHeader = request.getHeader("X-User-Id");
        UUID financeId = financeIdHeader != null ? UUID.fromString(financeIdHeader) : null;
        return ResponseEntity.ok(workflowService.getApprovalStatsByApprover(financeId));
    }
}