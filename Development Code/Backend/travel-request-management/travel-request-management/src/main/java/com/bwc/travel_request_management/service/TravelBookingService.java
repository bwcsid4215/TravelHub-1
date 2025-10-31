package com.bwc.travel_request_management.service;

import com.bwc.travel_request_management.dto.TravelBookingDTO;
import com.bwc.travel_request_management.dto.BookingSummaryDTO;

import java.util.List;
import java.util.UUID;

public interface TravelBookingService {
    TravelBookingDTO addBooking(UUID requestId, TravelBookingDTO bookingDto);
    TravelBookingDTO getBooking(UUID id);
    List<TravelBookingDTO> getBookingsForRequest(UUID requestId);
    void deleteBooking(UUID bookingId);
    TravelBookingDTO updateBookingStatus(UUID bookingId, String status);
    BookingSummaryDTO getBookingSummary(UUID requestId);
    Double getTotalBookingAmount(UUID requestId);
}