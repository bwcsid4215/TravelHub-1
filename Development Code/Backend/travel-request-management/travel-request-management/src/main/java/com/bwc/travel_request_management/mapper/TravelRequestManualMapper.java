package com.bwc.travel_request_management.mapper;

import com.bwc.travel_request_management.dto.TravelRequestDTO;
import com.bwc.travel_request_management.entity.TravelRequest;
import org.springframework.stereotype.Component;

@Component
public class TravelRequestManualMapper {

    public TravelRequestDTO toDto(TravelRequest entity) {
        if (entity == null) {
            return null;
        }

        return TravelRequestDTO.builder()
                .travelRequestId(entity.getTravelRequestId())
                .employeeId(entity.getEmployeeId())
                .projectId(entity.getProjectId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .purpose(entity.getPurpose())
                .managerPresent(entity.isManagerPresent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TravelRequest toEntity(TravelRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return TravelRequest.builder()
                .travelRequestId(dto.getTravelRequestId())
                .employeeId(dto.getEmployeeId())
                .projectId(dto.getProjectId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .purpose(dto.getPurpose())
                .managerPresent(dto.isManagerPresent())
                .build();
    }
}