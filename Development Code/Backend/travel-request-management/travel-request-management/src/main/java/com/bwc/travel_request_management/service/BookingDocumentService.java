package com.bwc.travel_request_management.service;

import com.bwc.travel_request_management.dto.BookingDocumentDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BookingDocumentService {
    BookingDocumentDTO uploadBookingDocument(UUID bookingId, MultipartFile file, 
                                           String documentType, String description, UUID uploadedBy);
    BookingDocumentDTO getDocument(UUID documentId);
    List<BookingDocumentDTO> getDocumentsByBooking(UUID bookingId);
    List<BookingDocumentDTO> getDocumentsByRequest(UUID requestId);
    List<BookingDocumentDTO> getDocumentsByType(UUID requestId, String documentType);
    void deleteDocument(UUID documentId);
    void deleteAllDocumentsForBooking(UUID bookingId);
    void deleteAllDocumentsForRequest(UUID requestId);
    Resource downloadDocument(UUID documentId); // Fixed: Using Spring Resource
    String getDocumentContentType(UUID documentId);
    String getDocumentOriginalName(UUID documentId);
}