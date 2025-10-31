package com.bwc.policymanagement.controller;

import com.bwc.policymanagement.dto.PolicyProxyFullDTO;
import com.bwc.policymanagement.service.PolicyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyProxyController {

    private final PolicyQueryService policyQueryService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<PolicyProxyFullDTO> getPolicyForEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String grade) {
        // If grade omitted, return active policy basic info
        return ResponseEntity.ok(policyQueryService.getPolicyByEmployee(employeeId, grade));
    }
}
