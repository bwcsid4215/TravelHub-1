package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelBookingDTO {
    private UUID bookingId;
    private BookingType bookingType;
    private String details;
    private String notes;
    private String bookingReference;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingDate;
    
    private Double bookingAmount;
    private String currency;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // For creation
    private UUID travelRequestId;
    private UUID workflowId;

    public enum BookingType {
        FLIGHT, HOTEL, TRAIN, CAR_RENTAL, OTHER
    }
}