package com.bwc.travel_request_management.repository;

import com.bwc.travel_request_management.entity.TravelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelBookingRepository extends JpaRepository<TravelBooking, UUID> {
    List<TravelBooking> findByTravelRequest_TravelRequestId(UUID requestId);
    
    @Query("SELECT SUM(b.bookingAmount) FROM TravelBooking b WHERE b.travelRequest.travelRequestId = :requestId AND b.bookingAmount IS NOT NULL")
    Double sumBookingAmountByRequestId(@Param("requestId") UUID requestId);
    
    List<TravelBooking> findByTravelRequest_TravelRequestIdAndStatus(UUID requestId, String status);
    
    Long countByTravelRequest_TravelRequestId(UUID requestId);
}