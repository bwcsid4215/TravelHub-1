package com.bwc.employee_management_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProjectResponse {
    private UUID projectId;
    private String projectName;
    private String description;
    private List<UUID> employeeIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}