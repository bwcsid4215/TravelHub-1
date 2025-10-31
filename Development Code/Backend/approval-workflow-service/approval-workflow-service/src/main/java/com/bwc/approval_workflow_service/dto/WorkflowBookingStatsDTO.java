package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowBookingStatsDTO {
    private UUID workflowId;
    private UUID travelRequestId;
    private Integer totalBookings;
    private Double totalBookingAmount;
    private Map<String, Integer> bookingsByType;
    private Map<String, Integer> bookingsByStatus;
    private Integer pendingBookings;
    private Integer confirmedBookings;
    private Integer cancelledBookings;
}