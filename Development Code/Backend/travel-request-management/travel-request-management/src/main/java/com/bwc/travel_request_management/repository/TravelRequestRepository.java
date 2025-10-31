package com.bwc.travel_request_management.repository;

import com.bwc.travel_request_management.entity.TravelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, UUID> {

    @Override
    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    Optional<TravelRequest> findById(UUID id);

    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    List<TravelRequest> findAll();

    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    Page<TravelRequest> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    List<TravelRequest> findByEmployeeId(UUID employeeId);

    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    Page<TravelRequest> findByEmployeeId(UUID employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    List<TravelRequest> findByProjectId(UUID projectId);

    @EntityGraph(attributePaths = {"expenses", "expenses.items", "bookings", "attachments"})
    Page<TravelRequest> findByProjectId(UUID projectId, Pageable pageable);

    @Query("SELECT tr FROM TravelRequest tr WHERE " +
           "tr.employeeId = :employeeId AND " +
           "((tr.startDate BETWEEN :startDate AND :endDate) OR " +
           "(tr.endDate BETWEEN :startDate AND :endDate) OR " +
           "(tr.startDate <= :startDate AND tr.endDate >= :endDate))")
    List<TravelRequest> findByEmployeeAndDateRange(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    long countByEmployeeId(UUID employeeId);

    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TravelRequest tr " +
           "WHERE tr.employeeId = :employeeId " +
           "AND tr.travelRequestId != :excludeId " +
           "AND ((tr.startDate BETWEEN :startDate AND :endDate) OR " +
           "(tr.endDate BETWEEN :startDate AND :endDate) OR " +
           "(tr.startDate <= :startDate AND tr.endDate >= :endDate))")
    boolean existsOverlappingRequestExcludingId(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId);

    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TravelRequest tr " +
           "WHERE tr.employeeId = :employeeId " +
           "AND ((tr.startDate BETWEEN :startDate AND :endDate) OR " +
           "(tr.endDate BETWEEN :startDate AND :endDate) OR " +
           "(tr.startDate <= :startDate AND tr.endDate >= :endDate))")
    boolean existsOverlappingRequest(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}