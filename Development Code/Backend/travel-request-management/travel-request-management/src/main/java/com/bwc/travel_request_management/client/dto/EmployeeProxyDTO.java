package com.bwc.travel_request_management.client.dto;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProxyDTO {
    private UUID employeeId;
    private String fullName;
    private String email;
    private String department;
    private String level;
    private UUID managerId;
    private Set<String> roles;
    private Set<UUID> projectIds; // ✅ Add this
}
