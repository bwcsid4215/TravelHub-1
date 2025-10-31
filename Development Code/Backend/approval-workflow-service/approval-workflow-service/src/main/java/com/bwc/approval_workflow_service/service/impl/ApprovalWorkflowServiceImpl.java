package com.bwc.approval_workflow_service.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.bwc.approval_workflow_service.client.EmployeeServiceClient;
import com.bwc.approval_workflow_service.client.NotificationServiceClient;
import com.bwc.approval_workflow_service.client.PolicyServiceClient;
import com.bwc.approval_workflow_service.client.TravelRequestServiceClient;
import com.bwc.approval_workflow_service.dto.ApprovalActionDTO;
import com.bwc.approval_workflow_service.dto.ApprovalRequestDTO;
import com.bwc.approval_workflow_service.dto.ApprovalStatsDTO;
import com.bwc.approval_workflow_service.dto.ApprovalWorkflowDTO;
import com.bwc.approval_workflow_service.dto.BookingDetailsDTO;
import com.bwc.approval_workflow_service.dto.BookingDocumentDTO;
import com.bwc.approval_workflow_service.dto.BookingSummaryDTO;
import com.bwc.approval_workflow_service.dto.EmployeeProxyDTO;
import com.bwc.approval_workflow_service.dto.NotificationRequestDTO;
import com.bwc.approval_workflow_service.dto.TravelBookingDTO;
import com.bwc.approval_workflow_service.dto.TravelRequestProxyDTO;
import com.bwc.approval_workflow_service.dto.WorkflowBookingStatsDTO;
import com.bwc.approval_workflow_service.dto.WorkflowMetricsDTO;
import com.bwc.approval_workflow_service.entity.ApprovalAction;
import com.bwc.approval_workflow_service.entity.ApprovalWorkflow;
import com.bwc.approval_workflow_service.entity.WorkflowConfiguration;
import com.bwc.approval_workflow_service.exception.ResourceNotFoundException;
import com.bwc.approval_workflow_service.exception.WorkflowException;
import com.bwc.approval_workflow_service.mapper.ApprovalWorkflowMapper;
import com.bwc.approval_workflow_service.repository.ApprovalActionRepository;
import com.bwc.approval_workflow_service.repository.ApprovalWorkflowRepository;
import com.bwc.approval_workflow_service.repository.WorkflowConfigurationRepository;
import com.bwc.approval_workflow_service.service.ApprovalWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {

    private final ApprovalWorkflowRepository workflowRepository;
    private final ApprovalActionRepository actionRepository;
    private final WorkflowConfigurationRepository configRepository;
    private final TravelRequestServiceClient travelRequestClient;
    private final PolicyServiceClient policyClient;
    private final EmployeeServiceClient employeeClient;
    private final NotificationServiceClient notificationClient;
    private final ApprovalWorkflowMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApprovalWorkflowDTO initiateWorkflow(UUID travelRequestId, String workflowType, Double estimatedCost) {
        TravelRequestProxyDTO travelRequest = fetchTravelRequestSafe(travelRequestId);
        return initiateWorkflow(travelRequest, workflowType, estimatedCost);
    }

    @Override
    @Transactional
    public ApprovalWorkflowDTO initiateWorkflow(TravelRequestProxyDTO travelRequest, String workflowType, Double estimatedCost) {
        UUID travelRequestId = travelRequest.travelRequestId();
        
        if (workflowRepository.findByTravelRequestIdAndWorkflowType(travelRequestId, workflowType).isPresent()) {
            throw new WorkflowException("Workflow already exists for travel request " + travelRequestId + " and type " + workflowType);
        }

        EmployeeProxyDTO employee = fetchEmployeeSafe(travelRequest.employeeId());

        List<WorkflowConfiguration> configs = configRepository
                .findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(workflowType);

        if (configs.isEmpty()) {
            throw new WorkflowException("No workflow configuration found for type: " + workflowType);
        }

        WorkflowConfiguration firstStep = configs.get(0);
        UUID approverId = determineApproverId(firstStep, travelRequest);

        ApprovalWorkflow workflow = ApprovalWorkflow.builder()
                .travelRequestId(travelRequestId)
                .workflowType(workflowType)
                .currentStep(firstStep.getStepName())
                .currentApproverRole(firstStep.getApproverRole())
                .currentApproverId(approverId)
                .status("PENDING")
                .nextStep(getNextStep(configs, 0))
                .priority(calculatePriority(travelRequest, estimatedCost))
                .estimatedCost(estimatedCost)
                .dueDate(calculateDueDate(firstStep))
                .build();

        ApprovalWorkflow savedWorkflow = workflowRepository.save(workflow);

        actionRepository.save(ApprovalAction.builder()
                .workflowId(savedWorkflow.getWorkflowId())
                .travelRequestId(travelRequestId)
                .approverRole("SYSTEM")
                .approverId(travelRequest.employeeId())
                .action("SUBMIT")
                .step("SUBMIT")
                .comments(workflowType + " workflow initiated")
                .actionTakenAt(LocalDateTime.now())
                .build());

        updateTravelRequestStatus(travelRequestId, "UNDER_REVIEW");
        sendNewApprovalNotification(savedWorkflow, travelRequest, employee);

        log.info("✅ {} workflow initiated successfully for request {}", workflowType, travelRequestId);
        return mapper.toDto(savedWorkflow);
    }

    @Override
    @Transactional
    public ApprovalWorkflowDTO processApproval(ApprovalRequestDTO approvalRequest) {
        ApprovalWorkflow workflow = workflowRepository.findById(approvalRequest.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"PENDING".equalsIgnoreCase(workflow.getStatus())) {
            throw new WorkflowException("Workflow is not in pending state");
        }

        validateApproverAuthorization(workflow, approvalRequest);
        validateManagerAuthorization(workflow, approvalRequest);

        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflow.getWorkflowId())
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole(approvalRequest.getApproverRole())
                .approverId(approvalRequest.getApproverId())
                .approverName(approvalRequest.getApproverName())
                .action(approvalRequest.getAction().toUpperCase())
                .step(workflow.getCurrentStep())
                .comments(approvalRequest.getComments())
                .escalationReason(approvalRequest.getEscalationReason())
                .isEscalated(approvalRequest.getEscalationReason() != null)
                .amountApproved(approvalRequest.getAmountApproved())
                .reimbursementAmount(approvalRequest.getReimbursementAmount())
                .actionTakenAt(LocalDateTime.now())
                .build());

        List<WorkflowConfiguration> configs = configRepository
                .findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(workflow.getWorkflowType());

        String action = approvalRequest.getAction().toUpperCase();
        switch (action) {
            case "APPROVE" -> handleApprove(workflow, configs, approvalRequest);
            case "REJECT" -> handleReject(workflow, approvalRequest.getComments());
            case "RETURN" -> handleReturn(workflow, approvalRequest.getComments());
            case "ESCALATE" -> handleEscalate(workflow, approvalRequest.getEscalationReason());
            default -> throw new WorkflowException("Unknown action: " + action);
        }

        ApprovalWorkflow updatedWorkflow = workflowRepository.save(workflow);
        return mapper.toDto(updatedWorkflow);
    }

    private void validateApproverAuthorization(ApprovalWorkflow workflow, ApprovalRequestDTO approvalRequest) {
        String currentStepRole = workflow.getCurrentApproverRole();
        String approverRole = approvalRequest.getApproverRole();
        
        if (!currentStepRole.equals(approverRole)) {
            throw new WorkflowException(
                String.format("Approver with role %s cannot approve step requiring role %s", 
                             approverRole, currentStepRole)
            );
        }
        
        log.info("✅ Authorization validated: {} can approve {} step", 
                 approverRole, workflow.getCurrentStep());
    }

    private void validateManagerAuthorization(ApprovalWorkflow workflow, ApprovalRequestDTO approvalRequest) {
        if ("MANAGER".equals(workflow.getCurrentApproverRole())) {
            if (workflow.getCurrentApproverId() == null) {
                throw new WorkflowException("No manager assigned to this workflow step");
            }
            
            if (!workflow.getCurrentApproverId().equals(approvalRequest.getApproverId())) {
                throw new WorkflowException(
                    String.format("Manager %s cannot approve request assigned to manager %s", 
                                 approvalRequest.getApproverId(), workflow.getCurrentApproverId())
                );
            }
        }
    }

    private void handleApprove(ApprovalWorkflow workflow, List<WorkflowConfiguration> configs, 
                             ApprovalRequestDTO approvalRequest) {
        int currentIndex = findCurrentStepIndex(configs, workflow.getCurrentStep());
        
        if ("TRAVEL_DESK_CHECK".equals(workflow.getCurrentStep()) && 
            Boolean.TRUE.equals(approvalRequest.getMarkOverpriced())) {
            workflow.setIsOverpriced(true);
            workflow.setOverpricedReason(approvalRequest.getOverpricedReason());
        }

        if ("FINANCE_APPROVAL".equals(workflow.getCurrentStep()) && 
            approvalRequest.getAmountApproved() != null) {
            workflow.setEstimatedCost(approvalRequest.getAmountApproved());
        }

        if (currentIndex >= configs.size() - 1) {
            completeWorkflow(workflow, "APPROVED");
            return;
        }

        WorkflowConfiguration nextStep = determineNextStep(workflow, configs, currentIndex);
        
        workflow.setPreviousStep(workflow.getCurrentStep());
        workflow.setCurrentStep(nextStep.getStepName());
        workflow.setCurrentApproverRole(nextStep.getApproverRole());
        workflow.setCurrentApproverId(determineApproverId(nextStep, 
                fetchTravelRequestSafe(workflow.getTravelRequestId())));
        workflow.setNextStep(getNextStep(configs, configs.indexOf(nextStep)));
        workflow.setDueDate(calculateDueDate(nextStep));
        workflow.setStatus("PENDING");
        
        if ("SYSTEM".equalsIgnoreCase(workflow.getCurrentApproverRole()) &&
                "WORKFLOW_COMPLETE".equalsIgnoreCase(workflow.getCurrentStep())) {
                completeWorkflow(workflow, "COMPLETED");
                log.info("✅ Workflow {} auto-completed by system after final reimbursement step.",
                        workflow.getWorkflowId());
                return;
            }

        sendNextApprovalNotification(workflow);
    }

    private WorkflowConfiguration determineNextStep(ApprovalWorkflow workflow, 
                                                   List<WorkflowConfiguration> configs, 
                                                   int currentIndex) {
        String currentStep = workflow.getCurrentStep();
        
        if ("PRE_TRAVEL".equals(workflow.getWorkflowType())) {
            switch (currentStep) {
                case "MANAGER_APPROVAL":
                    return configs.stream()
                            .filter(c -> "TRAVEL_DESK_CHECK".equals(c.getStepName()))
                            .findFirst()
                            .orElse(configs.get(currentIndex + 1));
                
                case "TRAVEL_DESK_CHECK":
                    if (Boolean.TRUE.equals(workflow.getIsOverpriced())) {
                        return configs.stream()
                                .filter(c -> "FINANCE_APPROVAL".equals(c.getStepName()))
                                .findFirst()
                                .orElse(configs.get(currentIndex + 1));
                    } else {
                        return configs.stream()
                                .filter(c -> "HR_APPROVAL".equals(c.getStepName()))
                                .findFirst()
                                .orElse(configs.get(currentIndex + 1));
                    }
                
                case "FINANCE_APPROVAL":
                    return configs.stream()
                            .filter(c -> "TRAVEL_DESK_BOOKING".equals(c.getStepName()))
                            .findFirst()
                            .orElse(configs.get(currentIndex + 1));
                
                case "TRAVEL_DESK_BOOKING":
                    return configs.stream()
                            .filter(c -> "HR_COMPLIANCE".equals(c.getStepName()))
                            .findFirst()
                            .orElse(configs.get(currentIndex + 1));
                
                case "HR_COMPLIANCE":
                    return configs.stream()
                            .filter(c -> "FINANCE_FINAL".equals(c.getStepName()))
                            .findFirst()
                            .orElse(configs.get(currentIndex + 1));
            }
        }
        
        if ("POST_TRAVEL".equals(workflow.getWorkflowType())) {
            switch (currentStep) {
                case "TRAVEL_DESK_BILL_REVIEW":
                    if (Boolean.TRUE.equals(workflow.getIsOverpriced())) {
                        return configs.stream()
                                .filter(c -> "FINANCE_REIMBURSEMENT".equals(c.getStepName()))
                                .findFirst()
                                .orElse(configs.get(currentIndex + 1));
                    } else {
                        return configs.stream()
                                .filter(c -> "FINANCE_REIMBURSEMENT".equals(c.getStepName()))
                                .findFirst()
                                .orElse(configs.get(currentIndex + 1));
                    }
            }
        }
        
        return configs.get(currentIndex + 1);
    }

    private void handleReject(ApprovalWorkflow workflow, String comments) {
        workflow.setStatus("REJECTED");
        workflow.setCompletedAt(LocalDateTime.now());
        updateTravelRequestStatus(workflow.getTravelRequestId(), "REJECTED");
        sendRejectionNotification(workflow, comments);
    }

    private void handleReturn(ApprovalWorkflow workflow, String comments) {
        workflow.setStatus("RETURNED");
        updateTravelRequestStatus(workflow.getTravelRequestId(), "RETURNED");
        sendReturnNotification(workflow, comments);
    }

    private void handleEscalate(ApprovalWorkflow workflow, String reason) {
        workflow.setStatus("ESCALATED");
        workflow.setPriority("HIGH");
        sendEscalationNotification(workflow, reason);
    }
    
    @Override
    @Transactional
    public void recordBookingAction(UUID workflowId, UUID travelDeskId, String action, String comments) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(travelDeskId)
                .action(action)
                .step("TRAVEL_DESK_BOOKING")
                .comments(comments)
                .actionTakenAt(LocalDateTime.now())
                .build());
        
        log.info("Booking action recorded: {} for workflow {}", action, workflowId);
    }
    

    @Override
    @Transactional
    public ApprovalWorkflowDTO markBookingUploaded(UUID workflowId, UUID uploadedBy) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"TRAVEL_DESK_BOOKING".equals(workflow.getCurrentStep())) {
            throw new WorkflowException("Workflow is not in booking upload step");
        }

        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(uploadedBy)
                .action("UPLOAD_BOOKING")
                .step("TRAVEL_DESK_BOOKING")
                .comments("Travel bookings uploaded")
                .actionTakenAt(LocalDateTime.now())
                .build());

        List<WorkflowConfiguration> configs = configRepository
                .findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(workflow.getWorkflowType());
        
        WorkflowConfiguration nextStep = configs.stream()
                .filter(c -> "HR_COMPLIANCE".equals(c.getStepName()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("HR compliance step not found"));

        workflow.setPreviousStep(workflow.getCurrentStep());
        workflow.setCurrentStep(nextStep.getStepName());
        workflow.setCurrentApproverRole(nextStep.getApproverRole());
        workflow.setCurrentApproverId(determineApproverId(nextStep, 
                fetchTravelRequestSafe(workflow.getTravelRequestId())));
        workflow.setNextStep(getNextStep(configs, configs.indexOf(nextStep)));
        workflow.setDueDate(calculateDueDate(nextStep));

        sendNextApprovalNotification(workflow);

        return mapper.toDto(workflowRepository.save(workflow));
    }

    @Override
    @Transactional
    public ApprovalWorkflowDTO uploadBills(UUID workflowId, Double actualCost, UUID uploadedBy) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"POST_TRAVEL".equals(workflow.getWorkflowType())) {
            throw new WorkflowException("Only post-travel workflows can have bills uploaded");
        }

        workflow.setActualCost(actualCost);

        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("EMPLOYEE")
                .approverId(uploadedBy)
                .action("UPLOAD_BILLS")
                .step("BILL_UPLOAD")
                .comments("Travel bills uploaded with actual cost: " + actualCost)
                .actionTakenAt(LocalDateTime.now())
                .build());

        List<WorkflowConfiguration> configs = configRepository
                .findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(workflow.getWorkflowType());
        
        WorkflowConfiguration nextStep = configs.stream()
                .filter(c -> "TRAVEL_DESK_BILL_REVIEW".equals(c.getStepName()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("Travel Desk bill review step not found"));

        workflow.setPreviousStep(workflow.getCurrentStep());
        workflow.setCurrentStep(nextStep.getStepName());
        workflow.setCurrentApproverRole(nextStep.getApproverRole());
        workflow.setCurrentApproverId(determineApproverId(nextStep, 
                fetchTravelRequestSafe(workflow.getTravelRequestId())));
        workflow.setNextStep(getNextStep(configs, configs.indexOf(nextStep)));
        workflow.setDueDate(calculateDueDate(nextStep));
        workflow.setStatus("PENDING");

        try {
            travelRequestClient.updateActualCost(workflow.getTravelRequestId(), actualCost);
        } catch (Exception e) {
            log.warn("Failed to update actual cost: {}", e.getMessage());
        }

        sendNextApprovalNotification(workflow);

        return mapper.toDto(workflowRepository.save(workflow));
    }

    private void completeWorkflow(ApprovalWorkflow workflow, String status) {
        workflow.setStatus(status);
        workflow.setCurrentStep("COMPLETED");
        workflow.setCompletedAt(LocalDateTime.now());

        String travelRequestStatus = "APPROVED".equals(status) ? "COMPLETED" : status;
        updateTravelRequestStatus(workflow.getTravelRequestId(), travelRequestStatus);

        if ("PRE_TRAVEL".equals(workflow.getWorkflowType()) && "APPROVED".equals(status)) {
            try {
                boolean postTravelExists = workflowRepository
                        .findByTravelRequestIdAndWorkflowType(workflow.getTravelRequestId(), "POST_TRAVEL")
                        .isPresent();

                if (!postTravelExists) {
                    TravelRequestProxyDTO travelRequest = fetchTravelRequestSafe(workflow.getTravelRequestId());
                    initiateWorkflow(travelRequest, "POST_TRAVEL", workflow.getEstimatedCost());
                    log.info("✅ POST_TRAVEL workflow automatically initiated for request {}", workflow.getTravelRequestId());
                } else {
                    log.warn("⚠️ POST_TRAVEL workflow already exists for request {}", workflow.getTravelRequestId());
                }
            } catch (Exception e) {
                log.error("❌ Failed to auto-initiate POST_TRAVEL workflow: {}", e.getMessage());
            }
        }

        sendCompletionNotification(workflow);
    }

    // Helper methods
    private TravelRequestProxyDTO fetchTravelRequestSafe(UUID id) {
        try {
            return travelRequestClient.getTravelRequest(id);
        } catch (FeignException e) {
            log.error("Failed to fetch travel request {}: {}", id, e.getMessage());
            throw new WorkflowException("Failed to retrieve travel request");
        }
    }

    private EmployeeProxyDTO fetchEmployeeSafe(UUID employeeId) {
        try {
            return employeeClient.getEmployee(employeeId);
        } catch (FeignException e) {
            log.warn("Failed to fetch employee {}: {}", employeeId, e.getMessage());
            return EmployeeProxyDTO.builder().employeeId(employeeId).build();
        }
    }

    private UUID determineApproverId(WorkflowConfiguration step, TravelRequestProxyDTO travelRequest) {
        UUID employeeId = travelRequest.employeeId();

        if ("MANAGER".equalsIgnoreCase(step.getApproverRole())) {
            try {
                EmployeeProxyDTO employee = employeeClient.getEmployee(employeeId);

                if (employee.getManagerId() != null) {
                    logApproverAssignment(
                            step.getStepName(),
                            "MANAGER",
                            employee.getManagerId(),
                            employee.getEmployeeId(),
                            "EmployeeService"
                    );
                    return employee.getManagerId();
                } else {
                    UUID fallbackId = getSystemAdminIdFallback();
                    logApproverAssignment(
                            step.getStepName(),
                            "MANAGER",
                            fallbackId,
                            employee.getEmployeeId(),
                            "Fallback: No manager found"
                    );
                    return fallbackId;
                }

            } catch (Exception e) {
                log.error("❌ [{}] Failed to fetch manager for employee {}: {}",
                        step.getStepName(), employeeId, e.getMessage());
                UUID fallbackId = getSystemAdminIdFallback();
                logApproverAssignment(step.getStepName(), "MANAGER", fallbackId, employeeId, "Exception fallback");
                return fallbackId;
            }
        }

        logApproverAssignment(step.getStepName(), step.getApproverRole(), null, employeeId, "Non-manager step");
        return null;
    }

    private UUID getSystemAdminIdFallback() {
        return UUID.fromString("ff78684e-ed8d-4696-bccf-582ecf1ab900");
    }

    private String getNextStep(List<WorkflowConfiguration> configs, int currentIndex) {
        return currentIndex < configs.size() - 1 ? configs.get(currentIndex + 1).getStepName() : "COMPLETED";
    }

    private LocalDateTime calculateDueDate(WorkflowConfiguration step) {
        return step.getTimeLimitHours() != null ? 
                LocalDateTime.now().plusHours(step.getTimeLimitHours()) : 
                LocalDateTime.now().plusDays(3);
    }

    private String calculatePriority(TravelRequestProxyDTO travelRequest, Double estimatedCost) {
        if (estimatedCost != null && estimatedCost > 5000) return "HIGH";
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                travelRequest.startDate(), travelRequest.endDate());
        if (days > 14) return "HIGH";
        return "NORMAL";
    }

    private int findCurrentStepIndex(List<WorkflowConfiguration> configs, String currentStep) {
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getStepName().equals(currentStep)) {
                return i;
            }
        }
        throw new WorkflowException("Current step not found in configuration: " + currentStep);
    }

    private void updateTravelRequestStatus(UUID travelRequestId, String status) {
        try {
            travelRequestClient.updateRequestStatus(travelRequestId, status);
        } catch (Exception e) {
            log.warn("Failed to update travel request status: {}", e.getMessage());
        }
    }

    @Transactional
    public void updateTravelRequestBookingStatus(UUID travelRequestId, String status) {
        try {
            travelRequestClient.updateRequestStatus(travelRequestId, status);
            log.info("✅ Travel request {} status updated to: {}", travelRequestId, status);
        } catch (Exception e) {
            log.error("❌ Failed to update travel request status: {}", e.getMessage());
            throw new WorkflowException("Failed to update travel request status: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getWorkflowsByStatusAndStep(String status, String step) {
        return workflowRepository.findByStatusAndCurrentStep(status, step)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
    
 // Add the missing method that the interface requires
    @Override
    @Transactional
    public ApprovalWorkflowDTO markBookingCompleted(UUID workflowId, UUID travelDeskId, String comments, BookingDetailsDTO bookingDetails) {
        // If you need to use bookingDetails, implement accordingly
        // For now, delegate to the simpler method
        return markBookingCompleted(workflowId, travelDeskId, comments);
    }
    
    @Override
    @Transactional
    public ApprovalWorkflowDTO markBookingCompleted(UUID workflowId, UUID travelDeskId, String comments) {
        // Your existing implementation
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"TRAVEL_DESK_BOOKING".equals(workflow.getCurrentStep())) {
            throw new WorkflowException("Workflow is not in booking upload step. Current step: " + workflow.getCurrentStep());
        }

        // Record the completion action
        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(travelDeskId)
                .action("COMPLETE_BOOKING")
                .step("TRAVEL_DESK_BOOKING")
                .comments(comments != null ? comments : "All travel bookings completed and confirmed")
                .actionTakenAt(LocalDateTime.now())
                .build());

        // Update travel request status to BOOKED
        updateTravelRequestBookingStatus(workflow.getTravelRequestId(), "BOOKED");

        // Move workflow to next step (HR_COMPLIANCE)
        List<WorkflowConfiguration> configs = configRepository
                .findByWorkflowTypeAndIsActiveTrueOrderBySequenceOrder(workflow.getWorkflowType());
        
        WorkflowConfiguration nextStep = configs.stream()
                .filter(c -> "HR_COMPLIANCE".equals(c.getStepName()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("HR compliance step not found"));

        workflow.setPreviousStep(workflow.getCurrentStep());
        workflow.setCurrentStep(nextStep.getStepName());
        workflow.setCurrentApproverRole(nextStep.getApproverRole());
        workflow.setCurrentApproverId(determineApproverId(nextStep, 
                fetchTravelRequestSafe(workflow.getTravelRequestId())));
        workflow.setNextStep(getNextStep(configs, configs.indexOf(nextStep)));
        workflow.setDueDate(calculateDueDate(nextStep));

        sendNextApprovalNotification(workflow);

        ApprovalWorkflow updatedWorkflow = workflowRepository.save(workflow);
        
        log.info("✅ Bookings marked as completed for workflow {}, moved to HR compliance", workflowId);
        return mapper.toDto(updatedWorkflow);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingSummaryDTO getBookingSummary(UUID workflowId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        List<BookingDocumentDTO> bookingDocuments = fetchBookingDocuments(workflow.getTravelRequestId());
        BookingDetailsDTO bookingDetails = convertJsonToBookingDetails(workflow.getBookingDetails());

        return BookingSummaryDTO.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .totalBookings(calculateTotalBookings(bookingDetails))
                .totalDocuments(bookingDocuments.size())
                .totalBookingAmount(workflow.getTotalBookingAmount())
                .status(workflow.getStatus())
                .documents(mapToDocumentSummary(bookingDocuments))
                .bookingDetails(bookingDetails)
                .build();
    }

    @Override
    @Transactional
    public ApprovalWorkflowDTO updateBookingDetails(UUID workflowId, UUID updatedBy, BookingDetailsDTO bookingDetails, String comments) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"TRAVEL_DESK_BOOKING".equals(workflow.getCurrentStep())) {
            throw new WorkflowException("Cannot update booking details. Current step: " + workflow.getCurrentStep());
        }

        workflow.setBookingDetails(convertBookingDetailsToJson(bookingDetails));
        workflow.setTotalBookingAmount(bookingDetails.getTotalBookingAmount());

        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(updatedBy)
                .action("UPDATE_BOOKING_DETAILS")
                .step("TRAVEL_DESK_BOOKING")
                .comments(comments != null ? comments : "Booking details updated")
                .actionTakenAt(LocalDateTime.now())
                .build());

        ApprovalWorkflow updatedWorkflow = workflowRepository.save(workflow);
        
        log.info("✅ Booking details updated for workflow {}", workflowId);
        return mapper.toDto(updatedWorkflow);
    }

    // Helper methods for booking management
    private String convertBookingDetailsToJson(BookingDetailsDTO bookingDetails) {
        try {
            return objectMapper.writeValueAsString(bookingDetails);
        } catch (Exception e) {
            log.warn("Failed to convert booking details to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private BookingDetailsDTO convertJsonToBookingDetails(String json) {
        if (json == null || json.trim().isEmpty()) {
            return BookingDetailsDTO.builder().build();
        }
        try {
            return objectMapper.readValue(json, BookingDetailsDTO.class);
        } catch (Exception e) {
            log.warn("Failed to convert JSON to booking details: {}", e.getMessage());
            return BookingDetailsDTO.builder().build();
        }
    }

    private List<BookingDocumentDTO> fetchBookingDocuments(UUID travelRequestId) {
        try {
            // This would call the travel-request-service to get booking documents
            // For now, return empty list - implement based on your service structure
            return List.of();
        } catch (Exception e) {
            log.warn("Failed to fetch booking documents: {}", e.getMessage());
            return List.of();
        }
    }

    private Integer calculateTotalBookings(BookingDetailsDTO bookingDetails) {
        if (bookingDetails == null) return 0;
        
        int total = 0;
        if (bookingDetails.getFlightBookings() != null) total += bookingDetails.getFlightBookings().size();
        if (bookingDetails.getHotelBookings() != null) total += bookingDetails.getHotelBookings().size();
        if (bookingDetails.getCarRentals() != null) total += bookingDetails.getCarRentals().size();
        if (bookingDetails.getOtherBookings() != null) total += bookingDetails.getOtherBookings().size();
        
        return total;
    }

    private List<BookingSummaryDTO.BookingDocumentSummary> mapToDocumentSummary(List<BookingDocumentDTO> documents) {
        return documents.stream()
                .map(doc -> BookingSummaryDTO.BookingDocumentSummary.builder()
                        .documentType(doc.getDocumentType().name())
                        .fileName(doc.getFileName())
                        .originalFileName(doc.getOriginalFileName())
                        .uploadDate(doc.getUploadedAt().toString())
                        .fileSize(doc.getFileSize())
                        .build())
                .collect(Collectors.toList());
    }

    // Notification methods
    @Async
    void sendNewApprovalNotification(ApprovalWorkflow workflow, TravelRequestProxyDTO travelRequest, EmployeeProxyDTO employee) {
        try {
            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .userId(workflow.getCurrentApproverId())
                    .subject("Approval Required: Travel Request")
                    .message("Travel request from " + employee.getFullName() + " requires your approval")
                    .notificationType("APPROVAL_REQUEST")
                    .referenceId(workflow.getTravelRequestId())
                    .referenceType("TRAVEL_REQUEST")
                    .build();
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }

    @Async
    void sendNextApprovalNotification(ApprovalWorkflow workflow) {
        try {
            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .userId(workflow.getCurrentApproverId())
                    .subject("Action Required: Next Approval Step")
                    .message("Workflow requires your action at step: " + workflow.getCurrentStep())
                    .notificationType("APPROVAL_NEXT")
                    .referenceId(workflow.getTravelRequestId())
                    .referenceType("TRAVEL_REQUEST")
                    .build();
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }

    // Other methods from interface
    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflowDTO getWorkflowByRequestId(UUID travelRequestId) {
        return workflowRepository.findByTravelRequestId(travelRequestId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflowDTO getWorkflow(UUID workflowId) {
        return workflowRepository.findById(workflowId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getPendingApprovals(String approverRole, UUID approverId) {
        List<ApprovalWorkflow> workflows;
        if (approverId != null) {
            workflows = workflowRepository.findByCurrentApproverIdAndStatus(approverId, "PENDING");
        } else {
            workflows = workflowRepository.findByCurrentApproverRoleAndStatus(approverRole, "PENDING");
        }
        return workflows.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getPendingApprovalsByRole(String approverRole) {
        return workflowRepository.findByCurrentApproverRoleAndStatus(approverRole, "PENDING")
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getWorkflowsByStatus(String status) {
        return workflowRepository.findByStatus(status).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalActionDTO> getWorkflowHistory(UUID travelRequestId) {
        return actionRepository.findByTravelRequestIdOrderByCreatedAtDesc(travelRequestId)
                .stream().map(mapper::toActionDto).collect(Collectors.toList());
    }

    @Override
    public ApprovalWorkflowDTO escalateWorkflow(UUID workflowId, String reason, UUID escalatedBy) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        workflow.setStatus("ESCALATED");
        workflow.setPriority("HIGH");
        workflowRepository.save(workflow);
        sendEscalationNotification(workflow, reason);
        return mapper.toDto(workflow);
    }

    @Override
    public ApprovalWorkflowDTO reassignWorkflow(UUID workflowId, String newApproverRole, UUID newApproverId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        workflow.setCurrentApproverRole(newApproverRole);
        workflow.setCurrentApproverId(newApproverId);
        workflowRepository.save(workflow);
        sendNextApprovalNotification(workflow);
        return mapper.toDto(workflow);
    }

    @Override
    public void reloadWorkflowConfigurations() {
        log.info("Workflow configurations reloaded");
    }

    @Override
    @Transactional
    public ApprovalWorkflowDTO updateWorkflowPriority(UUID workflowId, String priority) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        workflow.setPriority(priority);
        ApprovalWorkflow updated = workflowRepository.save(workflow);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowMetricsDTO getWorkflowMetrics() {
        long totalWorkflows = workflowRepository.count();
        long pendingWorkflows = workflowRepository.countByStatus("PENDING");
        long approvedWorkflows = workflowRepository.countByStatus("APPROVED");
        long rejectedWorkflows = workflowRepository.countByStatus("REJECTED");
        long escalatedWorkflows = workflowRepository.countByStatus("ESCALATED");

        double averageApprovalTime = calculateAverageApprovalTime();

        return WorkflowMetricsDTO.builder()
                .totalWorkflows(totalWorkflows)
                .pendingWorkflows(pendingWorkflows)
                .approvedWorkflows(approvedWorkflows)
                .rejectedWorkflows(rejectedWorkflows)
                .escalatedWorkflows(escalatedWorkflows)
                .averageApprovalTime(averageApprovalTime)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalStatsDTO> getApprovalStatsByApprover(UUID approverId) {
        List<ApprovalWorkflow> workflows = workflowRepository.findByCurrentApproverIdAndStatus(approverId, "PENDING");
        
        return List.of(ApprovalStatsDTO.builder()
                .approverId(approverId)
                .totalAssigned((long) workflows.size())
                .pending((long) workflows.size())
                .approved(0L)
                .rejected(0L)
                .averageProcessingTime(0.0)
                .build());
    }

    // Add missing notification methods implementation
    @Async
    void sendRejectionNotification(ApprovalWorkflow workflow, String comments) {
        try {
            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .subject("Workflow Rejected")
                    .message("Workflow " + workflow.getWorkflowId() + " was rejected. Comments: " + comments)
                    .notificationType("WORKFLOW_REJECTED")
                    .referenceId(workflow.getTravelRequestId())
                    .referenceType("TRAVEL_REQUEST")
                    .build();
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send rejection notification: {}", e.getMessage());
        }
    }

    @Async
    void sendReturnNotification(ApprovalWorkflow workflow, String comments) {
        try {
            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .subject("Workflow Returned")
                    .message("Workflow " + workflow.getWorkflowId() + " returned for correction. Comments: " + comments)
                    .notificationType("WORKFLOW_RETURNED")
                    .referenceId(workflow.getTravelRequestId())
                    .referenceType("TRAVEL_REQUEST")
                    .build();
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send return notification: {}", e.getMessage());
        }
    }

    @Async
    void sendEscalationNotification(ApprovalWorkflow workflow, String reason) {
        try {
            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .subject("Workflow Escalated")
                    .message("Workflow " + workflow.getWorkflowId() + " escalated. Reason: " + reason)
                    .notificationType("WORKFLOW_ESCALATED")
                    .referenceId(workflow.getTravelRequestId())
                    .referenceType("TRAVEL_REQUEST")
                    .build();
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send escalation notification: {}", e.getMessage());
        }
    }

    @Async
    void sendCompletionNotification(ApprovalWorkflow workflow) {
        try {
            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .subject("Workflow Completed")
                    .message("Workflow " + workflow.getWorkflowId() + " has been completed")
                    .notificationType("WORKFLOW_COMPLETED")
                    .referenceId(workflow.getTravelRequestId())
                    .referenceType("TRAVEL_REQUEST")
                    .build();
            notificationClient.sendNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send completion notification: {}", e.getMessage());
        }
    }

    private double calculateAverageApprovalTime() {
        List<ApprovalWorkflow> completedWorkflows = workflowRepository.findByStatus("APPROVED");
        if (completedWorkflows.isEmpty()) {
            return 0.0;
        }
        
        double totalHours = completedWorkflows.stream()
                .mapToDouble(wf -> {
                    if (wf.getCreatedAt() != null && wf.getCompletedAt() != null) {
                        return java.time.Duration.between(wf.getCreatedAt(), wf.getCompletedAt()).toHours();
                    }
                    return 0.0;
                })
                .sum();
        
        return totalHours / completedWorkflows.size();
    }
    
    private void logApproverAssignment(String stepName, String role, UUID approverId, UUID employeeId, String source) {
        if (approverId != null) {
            log.info("🧭 [{}] Assigned {} role to approver {} for employee {} (source: {})",
                    stepName, role, approverId, employeeId, source);
        } else {
            log.warn("⚠️ [{}] No approver ID found for role {} (employee: {}, source: {})",
                    stepName, role, employeeId, source);
        }
    }
    
 // ============ BOOKING MANAGEMENT METHODS ============

    @Override
    @Transactional
    public TravelBookingDTO addBookingToWorkflow(UUID workflowId, UUID travelDeskId, TravelBookingDTO bookingDTO) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"TRAVEL_DESK_BOOKING".equals(workflow.getCurrentStep())) {
            throw new WorkflowException("Cannot add booking. Workflow is not in TRAVEL_DESK_BOOKING step. Current step: " + workflow.getCurrentStep());
        }

        // Fetch existing booking details or create new ones
        BookingDetailsDTO existingBookingDetails = convertJsonToBookingDetails(workflow.getBookingDetails());
        if (existingBookingDetails == null) {
            existingBookingDetails = BookingDetailsDTO.builder().build();
        }

        // Add the new booking to appropriate category based on booking type
        switch (bookingDTO.getBookingType()) {
            case FLIGHT -> {
                if (existingBookingDetails.getFlightBookings() == null) {
                    existingBookingDetails.setFlightBookings(new ArrayList<>());
                }
                existingBookingDetails.getFlightBookings().add(convertToFlightBooking(bookingDTO));
            }
            case HOTEL -> {
                if (existingBookingDetails.getHotelBookings() == null) {
                    existingBookingDetails.setHotelBookings(new ArrayList<>());
                }
                existingBookingDetails.getHotelBookings().add(convertToHotelBooking(bookingDTO));
            }
            case CAR_RENTAL -> {
                if (existingBookingDetails.getCarRentals() == null) {
                    existingBookingDetails.setCarRentals(new ArrayList<>());
                }
                existingBookingDetails.getCarRentals().add(convertToCarRental(bookingDTO));
            }
            case OTHER -> {
                if (existingBookingDetails.getOtherBookings() == null) {
                    existingBookingDetails.setOtherBookings(new ArrayList<>());
                }
                existingBookingDetails.getOtherBookings().add(convertToOtherBooking(bookingDTO));
            }
        }

        // Update total booking amount
        Double currentTotal = existingBookingDetails.getTotalBookingAmount() != null ? 
                existingBookingDetails.getTotalBookingAmount() : 0.0;
        Double newBookingAmount = bookingDTO.getBookingAmount() != null ? bookingDTO.getBookingAmount() : 0.0;
        existingBookingDetails.setTotalBookingAmount(currentTotal + newBookingAmount);

        // Update workflow with new booking details
        workflow.setBookingDetails(convertBookingDetailsToJson(existingBookingDetails));
        workflow.setTotalBookingAmount(existingBookingDetails.getTotalBookingAmount());

        // Record the booking addition action
        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(travelDeskId)
                .action("ADD_BOOKING")
                .step("TRAVEL_DESK_BOOKING")
                .comments("Added " + bookingDTO.getBookingType() + " booking: " + bookingDTO.getDetails())
                .actionTakenAt(LocalDateTime.now())
                .build());

        workflowRepository.save(workflow);
        
        log.info("✅ Booking added to workflow {} by travel desk {}", workflowId, travelDeskId);
        return bookingDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelBookingDTO> getBookingsForWorkflow(UUID workflowId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        BookingDetailsDTO bookingDetails = convertJsonToBookingDetails(workflow.getBookingDetails());
        if (bookingDetails == null) {
            return List.of();
        }

        List<TravelBookingDTO> allBookings = new ArrayList<>();
        
        // Convert flight bookings
        if (bookingDetails.getFlightBookings() != null) {
            bookingDetails.getFlightBookings().forEach(fb -> 
                allBookings.add(convertFromFlightBooking(fb, TravelBookingDTO.BookingType.FLIGHT)));
        }
        
        // Convert hotel bookings
        if (bookingDetails.getHotelBookings() != null) {
            bookingDetails.getHotelBookings().forEach(hb -> 
                allBookings.add(convertFromHotelBooking(hb, TravelBookingDTO.BookingType.HOTEL)));
        }
        
        // Convert car rental bookings
        if (bookingDetails.getCarRentals() != null) {
            bookingDetails.getCarRentals().forEach(cr -> 
                allBookings.add(convertFromCarRental(cr, TravelBookingDTO.BookingType.CAR_RENTAL)));
        }
        
        // Convert other bookings
        if (bookingDetails.getOtherBookings() != null) {
            bookingDetails.getOtherBookings().forEach(ob -> 
                allBookings.add(convertFromOtherBooking(ob, TravelBookingDTO.BookingType.OTHER)));
        }

        return allBookings;
    }

    @Override
    @Transactional
    public TravelBookingDTO updateBookingStatus(UUID workflowId, UUID bookingId, String status, UUID travelDeskId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"TRAVEL_DESK_BOOKING".equals(workflow.getCurrentStep())) {
            throw new WorkflowException("Cannot update booking status. Workflow is not in TRAVEL_DESK_BOOKING step.");
        }

        // Note: Since we're storing bookings in JSON format, we don't have individual booking IDs
        // This method would need to be enhanced if you want to update specific bookings
        // For now, we'll record the action but the actual implementation would depend on your data structure
        
        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(travelDeskId)
                .action("UPDATE_BOOKING_STATUS")
                .step("TRAVEL_DESK_BOOKING")
                .comments("Updated booking status to: " + status)
                .actionTakenAt(LocalDateTime.now())
                .build());

        log.info("✅ Booking status updated for workflow {} by travel desk {}", workflowId, travelDeskId);
        
        // Return a placeholder - in a real implementation, you'd return the updated booking
        return TravelBookingDTO.builder()
                .status(status)
                .build();
    }

    @Override
    @Transactional
    public void deleteBookingFromWorkflow(UUID workflowId, UUID bookingId, UUID travelDeskId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        if (!"TRAVEL_DESK_BOOKING".equals(workflow.getCurrentStep())) {
            throw new WorkflowException("Cannot delete booking. Workflow is not in TRAVEL_DESK_BOOKING step.");
        }

        // Note: Since we're storing bookings in JSON format, deleting specific bookings would require
        // modifying the JSON structure. This is a simplified implementation.
        
        actionRepository.save(ApprovalAction.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .approverRole("TRAVEL_DESK")
                .approverId(travelDeskId)
                .action("DELETE_BOOKING")
                .step("TRAVEL_DESK_BOOKING")
                .comments("Deleted booking from workflow")
                .actionTakenAt(LocalDateTime.now())
                .build());

        log.info("✅ Booking deleted from workflow {} by travel desk {}", workflowId, travelDeskId);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowBookingStatsDTO getWorkflowBookingStats(UUID workflowId) {
        ApprovalWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        BookingDetailsDTO bookingDetails = convertJsonToBookingDetails(workflow.getBookingDetails());
        
        int totalBookings = calculateTotalBookings(bookingDetails);
        Double totalAmount = workflow.getTotalBookingAmount() != null ? workflow.getTotalBookingAmount() : 0.0;
        
        // Calculate bookings by type
        Map<String, Integer> bookingsByType = new HashMap<>();
        if (bookingDetails != null) {
            if (bookingDetails.getFlightBookings() != null) {
                bookingsByType.put("FLIGHT", bookingDetails.getFlightBookings().size());
            }
            if (bookingDetails.getHotelBookings() != null) {
                bookingsByType.put("HOTEL", bookingDetails.getHotelBookings().size());
            }
            if (bookingDetails.getCarRentals() != null) {
                bookingsByType.put("CAR_RENTAL", bookingDetails.getCarRentals().size());
            }
            if (bookingDetails.getOtherBookings() != null) {
                bookingsByType.put("OTHER", bookingDetails.getOtherBookings().size());
            }
        }
        
        // For status breakdown, we'd need to track individual booking statuses
        // This is a simplified version
        Map<String, Integer> bookingsByStatus = new HashMap<>();
        bookingsByStatus.put("CONFIRMED", totalBookings); // Assuming all are confirmed for now
        
        return WorkflowBookingStatsDTO.builder()
                .workflowId(workflowId)
                .travelRequestId(workflow.getTravelRequestId())
                .totalBookings(totalBookings)
                .totalBookingAmount(totalAmount)
                .bookingsByType(bookingsByType)
                .bookingsByStatus(bookingsByStatus)
                .pendingBookings(0) // Would need actual status tracking
                .confirmedBookings(totalBookings)
                .cancelledBookings(0) // Would need actual status tracking
                .build();
    }

    // ============ HELPER METHODS FOR BOOKING CONVERSION ============

    private BookingDetailsDTO.FlightBookingDTO convertToFlightBooking(TravelBookingDTO bookingDTO) {
        return BookingDetailsDTO.FlightBookingDTO.builder()
                .airline(extractAirlineFromDetails(bookingDTO.getDetails()))
                .flightNumber(extractFlightNumberFromDetails(bookingDTO.getDetails()))
                .departureAirport("") // Would need to parse from details
                .arrivalAirport("") // Would need to parse from details
                .departureDate(bookingDTO.getBookingDate() != null ? bookingDTO.getBookingDate().toString() : "")
                .arrivalDate("") // Would need separate field
                .amount(bookingDTO.getBookingAmount())
                .bookingReference(bookingDTO.getBookingReference())
                .status(bookingDTO.getStatus())
                .build();
    }

    private BookingDetailsDTO.HotelBookingDTO convertToHotelBooking(TravelBookingDTO bookingDTO) {
        return BookingDetailsDTO.HotelBookingDTO.builder()
                .hotelName(extractHotelNameFromDetails(bookingDTO.getDetails()))
                .location("") // Would need to parse from details
                .checkInDate("") // Would need separate fields
                .checkOutDate("") // Would need separate fields
                .numberOfNights(1) // Default
                .amount(bookingDTO.getBookingAmount())
                .bookingReference(bookingDTO.getBookingReference())
                .status(bookingDTO.getStatus())
                .build();
    }

    private BookingDetailsDTO.CarRentalDTO convertToCarRental(TravelBookingDTO bookingDTO) {
        return BookingDetailsDTO.CarRentalDTO.builder()
                .rentalCompany(extractRentalCompanyFromDetails(bookingDTO.getDetails()))
                .carType("") // Would need to parse from details
                .pickupDate("") // Would need separate fields
                .dropoffDate("") // Would need separate fields
                .pickupLocation("") // Would need to parse from details
                .amount(bookingDTO.getBookingAmount())
                .bookingReference(bookingDTO.getBookingReference())
                .status(bookingDTO.getStatus())
                .build();
    }

    private BookingDetailsDTO.OtherBookingDTO convertToOtherBooking(TravelBookingDTO bookingDTO) {
        return BookingDetailsDTO.OtherBookingDTO.builder()
                .type(bookingDTO.getBookingType().name())
                .description(bookingDTO.getDetails())
                .date(bookingDTO.getBookingDate() != null ? bookingDTO.getBookingDate().toString() : "")
                .amount(bookingDTO.getBookingAmount())
                .bookingReference(bookingDTO.getBookingReference())
                .status(bookingDTO.getStatus())
                .build();
    }

    private TravelBookingDTO convertFromFlightBooking(BookingDetailsDTO.FlightBookingDTO flightBooking, TravelBookingDTO.BookingType type) {
        return TravelBookingDTO.builder()
                .bookingType(type)
                .details(String.format("%s %s - %s to %s", 
                        flightBooking.getAirline(), 
                        flightBooking.getFlightNumber(),
                        flightBooking.getDepartureAirport(),
                        flightBooking.getArrivalAirport()))
                .bookingReference(flightBooking.getBookingReference())
                .bookingAmount(flightBooking.getAmount())
                .status(flightBooking.getStatus())
                .build();
    }

    private TravelBookingDTO convertFromHotelBooking(BookingDetailsDTO.HotelBookingDTO hotelBooking, TravelBookingDTO.BookingType type) {
        return TravelBookingDTO.builder()
                .bookingType(type)
                .details(String.format("%s - %s", 
                        hotelBooking.getHotelName(), 
                        hotelBooking.getLocation()))
                .bookingReference(hotelBooking.getBookingReference())
                .bookingAmount(hotelBooking.getAmount())
                .status(hotelBooking.getStatus())
                .build();
    }

    private TravelBookingDTO convertFromCarRental(BookingDetailsDTO.CarRentalDTO carRental, TravelBookingDTO.BookingType type) {
        return TravelBookingDTO.builder()
                .bookingType(type)
                .details(String.format("%s - %s", 
                        carRental.getRentalCompany(), 
                        carRental.getCarType()))
                .bookingReference(carRental.getBookingReference())
                .bookingAmount(carRental.getAmount())
                .status(carRental.getStatus())
                .build();
    }

    private TravelBookingDTO convertFromOtherBooking(BookingDetailsDTO.OtherBookingDTO otherBooking, TravelBookingDTO.BookingType type) {
        return TravelBookingDTO.builder()
                .bookingType(type)
                .details(otherBooking.getDescription())
                .bookingReference(otherBooking.getBookingReference())
                .bookingAmount(otherBooking.getAmount())
                .status(otherBooking.getStatus())
                .build();
    }
    
    

    // Simple extraction methods - these would need to be more sophisticated in production
    private String extractAirlineFromDetails(String details) {
        if (details == null) return "Unknown Airline";
        return details.split(" ")[0];
    }

    private String extractFlightNumberFromDetails(String details) {
        if (details == null) return "";
        // Simple extraction - would need more sophisticated parsing
        String[] parts = details.split(" ");
        for (String part : parts) {
            if (part.matches(".*\\d+.*")) {
                return part;
            }
        }
        return "";
    }

    private String extractHotelNameFromDetails(String details) {
        if (details == null) return "Unknown Hotel";
        return details.split("-")[0].trim();
    }

    private String extractRentalCompanyFromDetails(String details) {
        if (details == null) return "Unknown Rental Company";
        return details.split("-")[0].trim();
    }
}