package com.bwc.approval_workflow_service.client;

import com.bwc.approval_workflow_service.dto.PolicyProxyFullDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

@FeignClient(name = "policy-service", url = "${services.policy.url}")
public interface PolicyServiceClient {
    @GetMapping("/api/policies/employee/{employeeId}")
    PolicyProxyFullDTO getPolicyByEmployee(@PathVariable("employeeId") UUID employeeId, 
                                         @RequestParam(required = false) String grade);
}