package com.bwc.travel_request_management.repository;

import com.bwc.travel_request_management.entity.TravelExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TravelExpenseRepository extends JpaRepository<TravelExpense, UUID> {
    List<TravelExpense> findByTravelRequest_TravelRequestId(UUID requestId);
}