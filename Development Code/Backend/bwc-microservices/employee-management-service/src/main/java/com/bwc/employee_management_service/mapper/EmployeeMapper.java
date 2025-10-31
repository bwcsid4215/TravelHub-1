package com.bwc.employee_management_service.mapper;

import com.bwc.employee_management_service.dto.EmployeeRequest;
import com.bwc.employee_management_service.dto.EmployeeResponse;
import com.bwc.employee_management_service.entity.Employee;
import com.bwc.employee_management_service.entity.Project;
import com.bwc.employee_management_service.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    // ✅ Converts EmployeeRequest → Employee entity
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "subordinates", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dateJoined", expression = "java(new java.util.Date())")
    @Mapping(target = "isActive", constant = "true")
    Employee toEntity(EmployeeRequest request);

    // ✅ Converts Employee entity → EmployeeResponse DTO
    @Mapping(target = "managerId", source = "manager.employeeId")
    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "managerName", source = "manager.fullName")
    @Mapping(target = "roleIds", source = "roles", qualifiedByName = "mapRolesToIds")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToNames") // 🔥 new
    @Mapping(target = "projectIds", source = "projects", qualifiedByName = "mapProjectsToIds")
    EmployeeResponse toResponse(Employee employee);

    // ✅ Map roles → Set<UUID>
    @Named("mapRolesToIds")
    default Set<UUID> mapRolesToIds(Set<Role> roles) {
        if (roles == null) return Collections.emptySet();
        return roles.stream()
                .map(Role::getRoleId)
                .collect(Collectors.toSet());
    }

    // ✅ Map roles → Set<String> (role names)
    @Named("mapRolesToNames")
    default Set<String> mapRolesToNames(Set<Role> roles) {
        if (roles == null) return Collections.emptySet();
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
    }

    // ✅ Map projects → Set<UUID>
    @Named("mapProjectsToIds")
    default Set<UUID> mapProjectsToIds(Set<Project> projects) {
        if (projects == null) return Collections.emptySet();
        return projects.stream()
                .map(Project::getProjectId)
                .collect(Collectors.toSet());
    }
}
