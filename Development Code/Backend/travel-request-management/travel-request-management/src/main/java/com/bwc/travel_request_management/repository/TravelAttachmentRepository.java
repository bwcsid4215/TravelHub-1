package com.bwc.travel_request_management.repository;

import com.bwc.travel_request_management.entity.TravelAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelAttachmentRepository extends JpaRepository<TravelAttachment, UUID> {
    List<TravelAttachment> findByTravelRequest_TravelRequestId(UUID requestId);
}