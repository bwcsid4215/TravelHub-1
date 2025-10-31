package com.bwc.policymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TravelModeRequest {
    
    @NotBlank(message = "Mode name is required")
    @Size(max = 50, message = "Mode name must not exceed 50 characters")
    @Schema(description = "Name of the travel mode", example = "Flight")
    private String modeName;
    
    @NotNull(message = "Allowed classes are required")
    @Size(min = 1, message = "At least one travel class is required")
    @Schema(description = "List of allowed travel classes")
    private List<String> allowedClasses;
}