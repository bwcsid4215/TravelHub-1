package com.bwc.policymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PolicyRequest {
    
    @NotNull(message = "Year is required")
    @Schema(description = "Policy year", example = "2024")
    private Integer year;
    
    @NotNull(message = "Category ID is required")
    @Schema(description = "ID of the city category")
    private UUID categoryId;
    
    @Valid
    @NotNull(message = "Grade policies are required")
    @Size(min = 1, message = "At least one grade policy is required")
    @Schema(description = "List of grade policies")
    private List<GradePolicyRequest> gradePolicies;
}