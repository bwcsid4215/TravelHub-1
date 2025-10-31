package com.bwc.policymanagement.controller;

import com.bwc.common.dto.ApiResponse;
import com.bwc.policymanagement.dto.PolicyRequest;
import com.bwc.policymanagement.dto.PolicyResponse;
import com.bwc.policymanagement.entity.GradePolicy;
import com.bwc.policymanagement.entity.Policy;
import com.bwc.policymanagement.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@Tag(name = "Policy Management", description = "APIs for managing travel policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @Operation(summary = "Create a policy for a category with multiple GradePolicies")
    public ResponseEntity<ApiResponse<Policy>> createPolicy(
            @Valid @RequestBody PolicyRequest request,
            HttpServletRequest servletRequest) {
        
        Policy policy = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Policy created successfully", policy)
                        .path(servletRequest.getRequestURI()));
    }

    @GetMapping
    @Operation(summary = "List all policies")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> getAllPolicies(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(policyService.getAllPoliciesDto())
                .path(request.getRequestURI()));
    }
    

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicyById(@PathVariable UUID id,
            HttpServletRequest request) {
    	return ResponseEntity.ok(ApiResponse.success(policyService.getPolicyByIdDto(id))
    			.path(request.getRequestURI()));
    }
    
    @GetMapping("/active/category/{cityCategoryId}")
    @Operation(summary = "Fetch active grade policy by city category and employee grade")
    public ResponseEntity<ApiResponse<GradePolicy>> getActivePolicyByCityCategory(
            @Parameter(description = "City category ID") @PathVariable UUID cityCategoryId,
            @Parameter(description = "Employee grade (L1-L5)") @RequestParam String grade,
            HttpServletRequest servletRequest) {

        GradePolicy gradePolicy = policyService.getActivePolicyByCityCategoryAndGrade(cityCategoryId, grade);

        return ResponseEntity.ok(ApiResponse.success(gradePolicy)
                .path(servletRequest.getRequestURI()));
    }

    
    @PutMapping("/{id}")
    @Operation(summary = "Update policy")
    public ResponseEntity<ApiResponse<Policy>> updatePolicy(
            @Parameter(description = "Policy ID") @PathVariable UUID id,
            @Valid @RequestBody PolicyRequest request,
            HttpServletRequest servletRequest) {
        
        Policy policy = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Policy updated successfully", policy)
                .path(servletRequest.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete policy")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(
            @Parameter(description = "Policy ID") @PathVariable UUID id,
            HttpServletRequest servletRequest) {
        
    	policyService.deletePolicy(id);
        return ResponseEntity.ok(
            ApiResponse.<Void>success("Policy deleted successfully", null)
                .path(servletRequest.getRequestURI())
        );
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate or deactivate policy")
    public ResponseEntity<ApiResponse<PolicyResponse>> activatePolicy(
            @Parameter(description = "Policy ID") @PathVariable UUID id,
            @Parameter(description = "Activation status") @RequestParam Boolean active,
            HttpServletRequest servletRequest) {

        // Activate or deactivate the policy
        Policy policyEntity = policyService.activatePolicy(id, active);

        // Convert to DTO to avoid recursive serialization
        PolicyResponse policyDto = policyService.getPolicyByIdDto(policyEntity.getId());

        String message = active ? "Policy activated successfully" : "Policy deactivated successfully";

        return ResponseEntity.ok(ApiResponse.success(message, policyDto)
                .path(servletRequest.getRequestURI()));
    }


    @GetMapping("/active")
    @Operation(summary = "Fetch active policy for a given city and employee grade or by city category and grade")
    public ResponseEntity<ApiResponse<GradePolicy>> getActivePolicy(
            @Parameter(description = "City name") @RequestParam(required = false) String city,
            @Parameter(description = "City category ID") @RequestParam(required = false) UUID cityCategory,
            @Parameter(description = "Employee grade (L1-L5)") @RequestParam String grade,
            HttpServletRequest servletRequest) {
        
        GradePolicy policy;
        if (city != null) {
            policy = policyService.getActivePolicyByCityAndGrade(city, grade);
        } else if (cityCategory != null) {
            policy = policyService.getActivePolicyByCityCategoryAndGrade(cityCategory, grade);
        } else {
            throw new IllegalArgumentException("Either city or cityCategory must be provided");
        }
        
        return ResponseEntity.ok(ApiResponse.success(policy)
                .path(servletRequest.getRequestURI()));
    }
}