package com.bwc.travel_request_management.service;

import com.bwc.travel_request_management.dto.TravelRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TravelRequestService {
    TravelRequestDTO createRequest(TravelRequestDTO dto);
    TravelRequestDTO getRequest(UUID id);
    List<TravelRequestDTO> getAllRequests();
    Page<TravelRequestDTO> getAllRequests(Pageable pageable);
    List<TravelRequestDTO> getRequestsByEmployee(UUID employeeId);
    Page<TravelRequestDTO> getRequestsByEmployee(UUID employeeId, Pageable pageable);
    List<TravelRequestDTO> getRequestsByProject(UUID projectId);
    Page<TravelRequestDTO> getRequestsByProject(UUID projectId, Pageable pageable);
    List<TravelRequestDTO> getRequestsByEmployeeAndDateRange(UUID employeeId, LocalDate startDate, LocalDate endDate);

    TravelRequestDTO updateRequest(UUID id, TravelRequestDTO dto);
    TravelRequestDTO patchRequest(UUID id, TravelRequestDTO dto);
    void deleteRequest(UUID id);

    boolean hasOverlappingRequest(UUID employeeId, LocalDate startDate, LocalDate endDate);
    boolean hasOverlappingRequest(UUID employeeId, LocalDate startDate, LocalDate endDate, UUID excludeId);
    long getRequestCountByEmployee(UUID employeeId);

    // NEW
    void updateStatus(UUID id, String status);
}
