package com.bwc.approval_workflow_service.controller;

import com.bwc.approval_workflow_service.dto.ApprovalRequestDTO;
import com.bwc.approval_workflow_service.dto.ApprovalWorkflowDTO;
import com.bwc.approval_workflow_service.service.ApprovalWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerApprovalController {

    private final ApprovalWorkflowService workflowService;

    @GetMapping("/approvals/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ApprovalWorkflowDTO>> getPendingApprovals(
            HttpServletRequest request) {

        // Extract manager ID from security context (set by gateway)
        String managerIdHeader = request.getHeader("X-User-Id");
        if (managerIdHeader == null) {
            throw new RuntimeException("Manager ID not found in request");
        }
        
        UUID managerId = UUID.fromString(managerIdHeader);
        List<ApprovalWorkflowDTO> approvals = workflowService.getPendingApprovals("MANAGER", managerId);

        return ResponseEntity.ok(approvals);
    }

    @PostMapping("/approvals/{workflowId}/action")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApprovalWorkflowDTO> takeManagerAction(
            @PathVariable UUID workflowId,
            @RequestBody ApprovalRequestDTO approvalRequest,
            HttpServletRequest request) {

        // Extract manager ID from security context (set by gateway)
        String managerIdHeader = request.getHeader("X-User-Id");
        if (managerIdHeader == null) {
            throw new RuntimeException("Manager ID not found in request");
        }
        
        UUID managerId = UUID.fromString(managerIdHeader);
        
        approvalRequest.setWorkflowId(workflowId);
        approvalRequest.setApproverRole("MANAGER");
        approvalRequest.setApproverId(managerId);
        
        return ResponseEntity.ok(workflowService.processApproval(approvalRequest));
    }
}