package com.bwc.employee_management_service.mapper;

import com.bwc.employee_management_service.dto.ProjectResponse;
import com.bwc.employee_management_service.entity.Project;

import java.util.stream.Collectors;

public class ProjectMapper {

    public static ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setProjectId(project.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setDescription(project.getDescription());

        // Map employees only to their IDs
        response.setEmployeeIds(
                project.getEmployees().stream()
                        .map(e -> e.getEmployeeId())
                        .collect(Collectors.toList())
        );
        return response;
    }
}
