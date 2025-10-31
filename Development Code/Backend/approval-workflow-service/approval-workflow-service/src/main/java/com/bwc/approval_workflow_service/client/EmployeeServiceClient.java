package com.bwc.approval_workflow_service.client;

import com.bwc.approval_workflow_service.dto.EmployeeProxyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "employee-service", url = "${services.employee.url}")
public interface EmployeeServiceClient {
    @GetMapping("/api/v1/employees/proxy/{id}")
    EmployeeProxyDTO getEmployee(@PathVariable("id") UUID id);
}