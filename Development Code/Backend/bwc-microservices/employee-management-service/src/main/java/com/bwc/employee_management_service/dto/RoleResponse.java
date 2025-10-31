package com.bwc.employee_management_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RoleResponse {
    private UUID roleId;
    private String roleName;
    private String description;
    private boolean isActive;
}
