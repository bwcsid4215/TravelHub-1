package com.bwc.travel_request_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelExpenseDTO {

    private UUID expenseId;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    private LocalDateTime createdAt;

    @Valid
    @Builder.Default
    private Set<ExpenseItemDTO> items = new HashSet<>();

    // Calculated field
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(ExpenseItemDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}