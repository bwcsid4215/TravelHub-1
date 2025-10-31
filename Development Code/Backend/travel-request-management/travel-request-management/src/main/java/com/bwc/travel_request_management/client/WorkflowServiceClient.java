package com.bwc.travel_request_management.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.bwc.travel_request_management.client.dto.CreateWorkflowRequest;
import com.bwc.travel_request_management.dto.TravelRequestProxyDTO;

@FeignClient(
    name = "approval-workflow-service",
    url = "${services.workflow.url:http://localhost:8088}",
    configuration = com.bwc.travel_request_management.config.FeignConfig.class
)
public interface WorkflowServiceClient {

    @PostMapping("/api/workflows/initiate")
    void createWorkflow(@RequestBody CreateWorkflowRequest request);
    
    @PostMapping("/api/workflows/initiate-with-travel-request")
    void createWorkflowWithTravelRequest(
        @RequestBody TravelRequestProxyDTO travelRequest,
        @RequestParam String workflowType,
        @RequestParam Double estimatedCost
    );

    // ✅ NEW: Notify workflow service about booking upload
    @PostMapping("/api/workflows/{workflowId}/upload-booking")
    void markBookingUploaded(@RequestParam UUID uploadedBy);
}