package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelRequestInfoDTO {
    private UUID travelRequestId;
    private UUID employeeId;
    private String employeeName;
    private String employeeDepartment;
    private UUID projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;
    private Double estimatedBudget;
    private String travelDestination;
    private UUID managerId;
    private UUID categoryId;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
