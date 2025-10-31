package com.bwc.travel_request_management.mapper;

import com.bwc.travel_request_management.dto.*;
import com.bwc.travel_request_management.entity.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TravelRequestMapper {

    // TravelRequest mappings
    TravelRequestDTO toDto(TravelRequest entity);
    TravelRequest toEntity(TravelRequestDTO dto);

    // TravelExpense mappings
    TravelExpenseDTO toDto(TravelExpense entity);
    TravelExpense toEntity(TravelExpenseDTO dto);

    // ExpenseItem mappings
    ExpenseItemDTO toDto(ExpenseItem entity);
    ExpenseItem toEntity(ExpenseItemDTO dto);

    // TravelBooking mappings
    TravelBookingDTO toDto(TravelBooking entity);
    TravelBooking toEntity(TravelBookingDTO dto);

    // TravelAttachment mappings
    TravelAttachmentDTO toDto(TravelAttachment entity);
    TravelAttachment toEntity(TravelAttachmentDTO dto);

    // Helper method for BigDecimal conversion (if using BigDecimal)
    default java.math.BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? java.math.BigDecimal.valueOf(value) : null;
    }

    default Double bigDecimalToDouble(java.math.BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}