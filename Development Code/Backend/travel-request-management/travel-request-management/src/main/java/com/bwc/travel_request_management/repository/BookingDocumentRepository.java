package com.bwc.travel_request_management.repository;

import com.bwc.travel_request_management.entity.BookingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingDocumentRepository extends JpaRepository<BookingDocument, UUID> {
    List<BookingDocument> findByTravelBooking_BookingId(UUID bookingId);
    List<BookingDocument> findByTravelRequest_TravelRequestId(UUID requestId);
    List<BookingDocument> findByTravelRequest_TravelRequestIdAndDocumentType(UUID requestId, BookingDocument.DocumentType documentType);
    void deleteByTravelBooking_BookingId(UUID bookingId);
    void deleteByTravelRequest_TravelRequestId(UUID requestId);
    Long countByTravelBooking_BookingId(UUID bookingId);
    Long countByTravelRequest_TravelRequestId(UUID requestId);
}