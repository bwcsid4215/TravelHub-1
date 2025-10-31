package com.bwc.employee_service.controller;

import com.bwc.employee_management_service.dto.EmployeeResponse;
import com.bwc.employee_management_service.service.EmployeeService;
import com.bwc.employee_service.dto.EmployeeProxyDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeProxyController {

    private final EmployeeService employeeService;

    private final Cache<UUID, EmployeeProxyDTO> employeeProxyCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

    @GetMapping("/proxy/{id}")
    public EmployeeProxyDTO getProxy(@PathVariable UUID id) {
        return employeeProxyCache.get(id, key -> {
            log.info("🔍 Cache miss → Fetching employee proxy for ID: {}", key);

            EmployeeResponse emp = employeeService.getEmployeeById(key);
            if (emp == null) {
                throw new RuntimeException("Employee not found with ID: " + key);
            }

            return EmployeeProxyDTO.builder()
                    .employeeId(emp.getEmployeeId())
                    .fullName(emp.getFullName())
                    .email(emp.getEmail())
                    .department(emp.getDepartment())
                    .level(emp.getLevel())
                    .managerId(emp.getManagerId())
                    .roles(emp.getRoles() != null ? emp.getRoles() : Set.of())
                    .projectIds(emp.getProjectIds() != null ? emp.getProjectIds() : Set.of())
                    .build();
        });
    }

    @GetMapping("/proxy/by-role")
    public List<EmployeeProxyDTO> getByRole(@RequestParam("role") String role) {
        log.info("🔍 Fetching employees by role: {}", role);

        List<EmployeeResponse> employees = employeeService.getEmployeesByRole(role);

        return employees.stream()
                .map((EmployeeResponse emp) -> EmployeeProxyDTO.builder()
                        .employeeId(emp.getEmployeeId())
                        .fullName(emp.getFullName())
                        .email(emp.getEmail())
                        .department(emp.getDepartment())
                        .level(emp.getLevel())
                        .managerId(emp.getManagerId())
                        .roles(emp.getRoles() != null ? emp.getRoles() : Set.of())
                        .projectIds(emp.getProjectIds() != null ? emp.getProjectIds() : Set.of())
                        .build())
                .collect(Collectors.toList());
    }

}
