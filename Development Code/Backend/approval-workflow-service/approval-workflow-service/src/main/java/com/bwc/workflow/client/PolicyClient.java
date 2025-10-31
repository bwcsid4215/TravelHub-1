package com.bwc.workflow.client;

import com.bwc.workflow.client.dto.GradePolicyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "policy-service", url = "${external.policy-service.base-url}")
public interface PolicyClient {

    @GetMapping("/api/policies/active")
    GradePolicyDto getActiveByCityCategoryOrCityAndGrade(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) UUID cityCategory,
            @RequestParam String grade
    );
}
