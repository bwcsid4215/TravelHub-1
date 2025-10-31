package com.bwc.policymanagement.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bwc.common.dto.ApiResponse;
import com.bwc.policymanagement.dto.AddGradePolicyRequest;
import com.bwc.policymanagement.dto.GradePolicyRequest;
import com.bwc.policymanagement.dto.GradePolicyResponse;
import com.bwc.policymanagement.service.PolicyGradeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/policies/grades")
@Tag(name = "Policy Grade Management", description = "APIs for managing grade policies inside a policy")
@RequiredArgsConstructor
public class PolicyGradeController {

    private final PolicyGradeService policyGradeService;

    @PostMapping
    @Operation(summary = "Add a new grade to a policy (or create policy if not exists)")
    public ResponseEntity<ApiResponse<GradePolicyResponse>> addGradePolicy(
            @Valid @RequestBody AddGradePolicyRequest request) {

        GradePolicyResponse gradePolicy = policyGradeService.addGradePolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("GradePolicy added successfully", gradePolicy));
    }

    @PutMapping("/{policyId}/{grade}")
    @Operation(summary = "Update an existing grade policy")
    public ResponseEntity<ApiResponse<GradePolicyResponse>> updateGradePolicy(
            @PathVariable UUID policyId,
            @PathVariable String grade,
            @Valid @RequestBody GradePolicyRequest request) {

        GradePolicyResponse updated = policyGradeService.updateGradePolicy(policyId, grade, request);
        return ResponseEntity.ok(ApiResponse.success("GradePolicy updated successfully", updated));
    }

    @GetMapping("/{policyId}")
    @Operation(summary = "List all grade policies for a policy")
    public ResponseEntity<ApiResponse<List<GradePolicyResponse>>> getGradePolicies(
            @PathVariable UUID policyId) {

        List<GradePolicyResponse> list = policyGradeService.getGradePolicies(policyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @DeleteMapping("/{policyId}/{grade}")
    @Operation(summary = "Delete a specific grade policy from a policy")
    public ResponseEntity<ApiResponse<Void>> deleteGradePolicy(
            @PathVariable UUID policyId,
            @PathVariable String grade) {

        policyGradeService.deleteGradePolicy(policyId, grade);
        return ResponseEntity.ok(ApiResponse.success("GradePolicy deleted successfully", null));
    }
}
