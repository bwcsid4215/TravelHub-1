package com.bwc.workflow.client.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class EmployeeDto {
    private UUID employeeId;
    private String fullName;
    private String email;
    private String department;
    private String level;
    private Boolean active;
    private UUID managerId;
    private Set<UUID> roleIds;
}
