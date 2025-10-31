package com.bwc.policymanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddGradePolicyRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Year is required")
    private Integer year;

    @Valid
    @NotNull(message = "GradePolicy is required")
    private GradePolicyRequest gradePolicy;
}
