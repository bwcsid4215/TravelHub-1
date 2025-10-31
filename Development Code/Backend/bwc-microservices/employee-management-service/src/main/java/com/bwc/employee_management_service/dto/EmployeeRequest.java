package com.bwc.employee_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request DTO for creating or updating an employee")
@Data
public class EmployeeRequest {
    
    @Schema(description = "Full name of the employee", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Schema(description = "Email address of the employee", example = "john.doe@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Schema(description = "Phone number in international format", example = "+1-555-0123")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
    private String phoneNumber;
    
    @Schema(description = "Department name", example = "Engineering", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 50, message = "Department must be between 2 and 50 characters")
    private String department;
    
    @Schema(description = "Job level or grade", example = "Senior")
    @Size(min = 2, max = 20, message = "Level must be between 2 and 20 characters")
    private String level;
    
    @Schema(description = "UUID of the manager", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID managerId;
    
    @Schema(description = "Set of role UUIDs assigned to the employee")
    private Set<UUID> roleIds;
    
    @Schema(description = "Set of project UUIDs assigned to the employee")
    private Set<UUID> projectIds;
}