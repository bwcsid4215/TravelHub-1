package com.bwc.workflow.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bwc.workflow.client.dto.EmployeeDto;
import com.bwc.workflow.client.dto.RoleResponseDto;

@FeignClient(name = "employee-service", url = "${external.employee-service.base-url}")
public interface EmployeeClient {

    @GetMapping("/api/v1/employees/{id}")
    EmployeeDto getEmployeeById(@PathVariable("id") UUID id);

    @GetMapping("/api/v1/employees/role/{roleName}")
    List<EmployeeDto> getEmployeesByRoleName(@PathVariable("roleName") String roleName);

    @GetMapping("/api/v1/roles/name/{roleName}")
    RoleResponseDto getRoleByName(@PathVariable("roleName") String roleName);
}
