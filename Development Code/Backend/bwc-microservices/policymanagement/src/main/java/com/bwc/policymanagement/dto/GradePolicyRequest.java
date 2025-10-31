package com.bwc.policymanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradePolicyRequest {
    
    @NotBlank(message = "Grade is required")
    @Size(min = 1, max = 10, message = "Grade must be between 1 and 10 characters")
    private String grade;
    
    @NotNull(message = "Company rate is required")
    @PositiveOrZero(message = "Company rate must be positive or zero")
    private Double companyRate;
    
    @NotNull(message = "Own rate is required")
    @PositiveOrZero(message = "Own rate must be positive or zero")
    private Double ownRate;
    
    @NotBlank(message = "Overnight rule is required")
    @Size(max = 500, message = "Overnight rule must not exceed 500 characters")
    private String overnightRule;
    
    @NotBlank(message = "Day trip rule is required")
    @Size(max = 500, message = "Day trip rule must not exceed 500 characters")
    private String dayTripRule;
    
    @Valid
    @NotNull(message = "Travel modes are required")
    @Size(min = 1, message = "At least one travel mode is required")
    private List<TravelModeRequest> travelModes;
}