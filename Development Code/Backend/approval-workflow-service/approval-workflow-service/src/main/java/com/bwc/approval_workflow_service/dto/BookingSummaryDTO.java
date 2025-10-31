package com.bwc.approval_workflow_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingSummaryDTO {
    private UUID workflowId;
    private UUID travelRequestId;
    private Integer totalBookings;
    private Integer totalDocuments;
    private Double totalBookingAmount;
    private String status;
    private List<BookingDocumentSummary> documents;
    private BookingDetailsDTO bookingDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingDocumentSummary {
        private String documentType;
        private String fileName;
        private String originalFileName;
        private String uploadDate;
        private Long fileSize;
    }
}