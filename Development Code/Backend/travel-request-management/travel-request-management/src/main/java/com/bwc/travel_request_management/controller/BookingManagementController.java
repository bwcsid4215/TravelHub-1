package com.bwc.travel_request_management.controller;

import com.bwc.travel_request_management.dto.BookingDocumentDTO;
import com.bwc.travel_request_management.dto.BookingSummaryDTO;
import com.bwc.travel_request_management.dto.TravelBookingDTO;
import com.bwc.travel_request_management.service.BookingDocumentService;
import com.bwc.travel_request_management.service.TravelBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Booking Management", description = "Manage travel bookings and their related documents")
public class BookingManagementController {

    private final TravelBookingService bookingService;
    private final BookingDocumentService documentService;

    // ==========================================================
    // 🧳 BOOKING MANAGEMENT ENDPOINTS
    // ==========================================================

    @Operation(summary = "Add a travel booking for a request")
    @PostMapping("/{requestId}")
    public ResponseEntity<TravelBookingDTO> addBooking(
            @Parameter(description = "Travel request ID") @PathVariable UUID requestId,
            @Valid @RequestBody TravelBookingDTO dto) {

        log.info("Adding booking for requestId: {}", requestId);
        return ResponseEntity.ok(bookingService.addBooking(requestId, dto));
    }

    @Operation(summary = "Get booking by ID")
    @GetMapping("/{bookingId}")
    public ResponseEntity<TravelBookingDTO> getBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {

        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    @Operation(summary = "List all bookings for a travel request")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<List<TravelBookingDTO>> getBookingsByRequest(
            @Parameter(description = "Travel request ID") @PathVariable UUID requestId) {

        return ResponseEntity.ok(bookingService.getBookingsForRequest(requestId));
    }

    @Operation(summary = "Get booking summary for a travel request")
    @GetMapping("/summary/{requestId}")
    public ResponseEntity<BookingSummaryDTO> getBookingSummary(
            @Parameter(description = "Travel request ID") @PathVariable UUID requestId) {

        return ResponseEntity.ok(bookingService.getBookingSummary(requestId));
    }

    @Operation(summary = "Update booking status")
    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<TravelBookingDTO> updateStatus(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @RequestParam String status) {

        log.info("Updating status of booking {} to {}", bookingId, status);
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status));
    }

    @Operation(summary = "Delete a booking")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {

        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // 📎 BOOKING DOCUMENT MANAGEMENT ENDPOINTS
    // ==========================================================

    @Operation(summary = "Upload booking document")
    @PostMapping(value = "/{bookingId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookingDocumentDTO> uploadDocument(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader("X-User-Id") UUID uploadedBy) {

        log.info("Uploading document for booking: {}, type: {}, uploadedBy: {}", bookingId, documentType, uploadedBy);
        BookingDocumentDTO document = documentService.uploadBookingDocument(
                bookingId, file, documentType, description, uploadedBy);

        return ResponseEntity.ok(document);
    }

    @Operation(summary = "List all documents for a booking")
    @GetMapping("/{bookingId}/documents")
    public ResponseEntity<List<BookingDocumentDTO>> getDocumentsByBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {

        return ResponseEntity.ok(documentService.getDocumentsByBooking(bookingId));
    }

    @Operation(summary = "List all documents for a travel request")
    @GetMapping("/request/{requestId}/documents")
    public ResponseEntity<List<BookingDocumentDTO>> getDocumentsByRequest(
            @Parameter(description = "Travel request ID") @PathVariable UUID requestId) {

        return ResponseEntity.ok(documentService.getDocumentsByRequest(requestId));
    }

    @Operation(summary = "Download a booking document")
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "Document ID") @PathVariable UUID documentId) {

        Resource resource = documentService.downloadDocument(documentId);
        String contentType = documentService.getDocumentContentType(documentId);
        String originalFileName = documentService.getDocumentOriginalName(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                .body(resource);
    }

    @Operation(summary = "View a booking document inline")
    @GetMapping("/documents/{documentId}/view")
    public ResponseEntity<Resource> viewDocument(
            @Parameter(description = "Document ID") @PathVariable UUID documentId) {

        Resource resource = documentService.downloadDocument(documentId);
        String contentType = documentService.getDocumentContentType(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @Operation(summary = "Delete booking document")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable UUID documentId) {

        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
