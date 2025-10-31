package com.bwc.policymanagement.service;

import com.bwc.policymanagement.dto.PolicyRequest;
import com.bwc.policymanagement.dto.PolicyResponse;
import com.bwc.policymanagement.entity.GradePolicy;
import com.bwc.policymanagement.entity.Policy;

import java.util.List;
import java.util.UUID;

public interface PolicyService {
    
    // Existing entity-based methods
    Policy createPolicy(PolicyRequest request);
    GradePolicy getActivePolicyByCityAndGrade(String cityName, String grade);
    Policy getPolicyById(UUID id);
    List<Policy> getAllPolicies();
    Policy updatePolicy(UUID id, PolicyRequest request);
    void deletePolicy(UUID id);
    Policy activatePolicy(UUID id, Boolean active);
    GradePolicy getActivePolicyByCityCategoryAndGrade(UUID cityCategory, String grade);

    // --- New DTO-based methods ---
    List<PolicyResponse> getAllPoliciesDto();
    PolicyResponse getPolicyByIdDto(UUID id);
}
