package com.bwc.policymanagement.service;

import com.bwc.policymanagement.dto.AddGradePolicyRequest;
import com.bwc.policymanagement.dto.GradePolicyRequest;
import com.bwc.policymanagement.dto.GradePolicyResponse;

import java.util.List;
import java.util.UUID;

public interface PolicyGradeService {

    GradePolicyResponse addGradePolicy(AddGradePolicyRequest request);

    GradePolicyResponse updateGradePolicy(UUID policyId, String grade, GradePolicyRequest request);

    void deleteGradePolicy(UUID policyId, String grade);

    List<GradePolicyResponse> getGradePolicies(UUID policyId);
}
