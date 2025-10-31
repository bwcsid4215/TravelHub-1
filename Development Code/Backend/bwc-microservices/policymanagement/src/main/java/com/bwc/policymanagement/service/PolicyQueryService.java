package com.bwc.policymanagement.service;

import com.bwc.policymanagement.dto.PolicyProxyFullDTO;

import java.util.UUID;

public interface PolicyQueryService {
    PolicyProxyFullDTO getPolicyByEmployee(UUID employeeId, String grade);
}
