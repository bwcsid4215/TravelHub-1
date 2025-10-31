package com.bwc.employee_management_service.mapper;

import com.bwc.employee_management_service.dto.RoleResponse;
import com.bwc.employee_management_service.entity.Role;

public class RoleMapper {

    public static RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setRoleId(role.getRoleId());
        response.setRoleName(role.getRoleName());
        response.setDescription(role.getDescription());
        response.setActive(role.getIsActive());;
        return response;
    }
}
