package com.bwc.travel_request_management.dto;

import com.bwc.travel_request_management.entity.TravelBooking;
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

    private UUID travelRequestId;
    private Integer totalBookings;
    private Double totalBookingAmount;
    private List<TravelBookingDTO> bookings;
    private List<BookingDocumentDTO> documents;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingTypeSummary {
        private TravelBooking.BookingType bookingType;
        private Integer count;
        private Double totalAmount;
    }
}