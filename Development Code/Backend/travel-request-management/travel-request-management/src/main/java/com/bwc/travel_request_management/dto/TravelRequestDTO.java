package com.bwc.travel_request_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelRequestDTO {

    private UUID travelRequestId;

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @NotBlank(message="origin location is required")
    @Size(max = 1000, message = "Purpose cannot exceed 1000 characters")
    private String origin;
    
    @NotBlank(message="origin location is required")
    @Size(max = 1000, message = "Purpose cannot exceed 1000 characters")
    private String travelDestination;
    

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotBlank(message = "Purpose is required")
    @Size(max = 1000, message = "Purpose cannot exceed 1000 characters")
    private String purpose;

    @Builder.Default
    private boolean managerPresent = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Validation method
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null cases
        }
        return endDate.isAfter(startDate);
    }
    
    private Double estimatedBudget;
}