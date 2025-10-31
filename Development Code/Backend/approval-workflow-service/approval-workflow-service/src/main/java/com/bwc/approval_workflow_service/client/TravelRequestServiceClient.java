package com.bwc.approval_workflow_service.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bwc.approval_workflow_service.dto.BookingDocumentDTO;
import com.bwc.approval_workflow_service.dto.BookingSummaryDTO;
import com.bwc.approval_workflow_service.dto.TravelBookingDTO;
import com.bwc.approval_workflow_service.dto.TravelRequestProxyDTO;

@FeignClient(name = "travel-request-service", url = "${services.travel-request.url:http://localhost:8080}")
public interface TravelRequestServiceClient {

    // ==========================================================
    // 🧳 BOOKING MANAGEMENT ENDPOINTS
    // ==========================================================

    @PostMapping("/api/bookings/{requestId}")
    TravelBookingDTO addBooking(@PathVariable UUID requestId, @RequestBody TravelBookingDTO bookingDto);

    @GetMapping("/api/bookings/by-request/{requestId}")
    List<TravelBookingDTO> getBookingsForRequest(@PathVariable UUID requestId);

    @GetMapping("/api/bookings/{bookingId}")
    TravelBookingDTO getBooking(@PathVariable UUID bookingId);

    @PatchMapping("/api/bookings/{bookingId}/status")
    TravelBookingDTO updateBookingStatus(@PathVariable UUID bookingId, @RequestParam String status);

    @DeleteMapping("/api/bookings/{bookingId}")
    void deleteBooking(@PathVariable UUID bookingId);

    @GetMapping("/api/bookings/summary/{requestId}")
    BookingSummaryDTO getBookingSummary(@PathVariable UUID requestId);

    // ==========================================================
    // 📎 BOOKING DOCUMENT MANAGEMENT ENDPOINTS
    // ==========================================================

    @PostMapping(value = "/api/bookings/{bookingId}/documents/upload", consumes = "multipart/form-data")
    BookingDocumentDTO uploadBookingDocument(
            @PathVariable UUID bookingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("X-User-Id") UUID uploadedBy);

    @GetMapping("/api/bookings/{bookingId}/documents")
    List<BookingDocumentDTO> getDocumentsByBooking(@PathVariable UUID bookingId);

    @GetMapping("/api/bookings/request/{requestId}/documents")
    List<BookingDocumentDTO> getDocumentsByRequest(@PathVariable UUID requestId);

    @DeleteMapping("/api/bookings/documents/{documentId}")
    void deleteDocument(@PathVariable UUID documentId);

    // ==========================================================
    // 🔁 TRAVEL REQUEST (for workflow updates)
    // ==========================================================

    @PostMapping("/api/travel-requests/{id}/status")
    void updateRequestStatus(@PathVariable UUID id, @RequestParam String status);

    @GetMapping("/api/travel-requests/{id}")
    TravelRequestProxyDTO getTravelRequest(@PathVariable("id") UUID id);

    @PatchMapping("/api/travel-requests/{travelRequestId}/actual-cost")
    void updateActualCost(@PathVariable UUID travelRequestId, @RequestParam Double actualCost);

    @GetMapping("/api/travel-requests/proxy/{id}")
    TravelRequestProxyDTO getTravelRequestProxy(@PathVariable("id") UUID id);
}
