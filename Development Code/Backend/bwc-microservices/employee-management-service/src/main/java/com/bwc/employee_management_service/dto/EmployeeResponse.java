package com.bwc.employee_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "Response DTO containing employee details")
public class EmployeeResponse {

    @Schema(description = "Unique identifier of the employee", 
            example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID employeeId;

    @Schema(description = "Full name of the employee", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@company.com")
    private String email;

    @Schema(description = "Phone number", example = "+1-555-0123")
    private String phoneNumber;

    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @Schema(description = "Job level", example = "Senior")
    private String level;

    @Schema(description = "Indicates if the employee is active", example = "true")
    private boolean active;

    @Schema(description = "Manager's UUID", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID managerId;

    @Schema(description = "Manager's full name", example = "Jane Smith")
    private String managerName;

    @Schema(description = "Set of assigned role UUIDs")
    private Set<UUID> roleIds;

    @Schema(description = "Set of assigned role names", example = "[\"HR\", \"MANAGER\"]")
    private Set<String> roles;
    
    @Schema(description = "Set of assigned project UUIDs")
    private Set<UUID> projectIds;
    
    @Schema(description = "Creation timestamp", 
            example = "2023-12-07T10:30:00.000Z", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", 
            example = "2023-12-07T10:30:00.000Z", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
