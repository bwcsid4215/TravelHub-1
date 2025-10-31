package com.bwc.travel_request_management.mapper;

import com.bwc.travel_request_management.dto.TravelExpenseDTO;
import com.bwc.travel_request_management.dto.ExpenseItemDTO;
import com.bwc.travel_request_management.entity.TravelExpense;
import com.bwc.travel_request_management.entity.ExpenseItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TravelExpenseMapper {

    // Expense
    public TravelExpenseDTO toDto(TravelExpense entity) {
        if (entity == null) return null;
        TravelExpenseDTO dto = TravelExpenseDTO.builder()
                .expenseId(entity.getExpenseId())
                .expenseDate(entity.getExpenseDate())
                .createdAt(entity.getCreatedAt())
                .build();

        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                    .map(this::toDto)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    public TravelExpense toEntity(TravelExpenseDTO dto) {
        if (dto == null) return null;
        TravelExpense entity = TravelExpense.builder()
                .expenseId(dto.getExpenseId())
                .expenseDate(dto.getExpenseDate())
                .build();

        if (dto.getItems() != null) {
            entity.setItems(dto.getItems().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toSet()));
        }
        return entity;
    }

    // ExpenseItem
    public ExpenseItemDTO toDto(ExpenseItem entity) {
        if (entity == null) return null;
        return ExpenseItemDTO.builder()
                .itemId(entity.getItemId())
                .category(entity.getCategory())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ExpenseItem toEntity(ExpenseItemDTO dto) {
        if (dto == null) return null;
        return ExpenseItem.builder()
                .itemId(dto.getItemId())
                .category(dto.getCategory())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();
    }
}