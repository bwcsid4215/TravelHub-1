package com.bwc.policymanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityRequest {
    
    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    private String name;
    
    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description; // Add this field
}
