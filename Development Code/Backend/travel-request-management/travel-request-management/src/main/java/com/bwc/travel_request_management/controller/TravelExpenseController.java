package com.bwc.travel_request_management.controller;

import com.bwc.travel_request_management.dto.TravelExpenseDTO;
import com.bwc.travel_request_management.service.TravelExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/travel-expenses")
@RequiredArgsConstructor
@Validated
@Tag(name = "Travel Expenses", description = "Manage expenses related to travel requests")
public class TravelExpenseController {

    private final TravelExpenseService service;

    @Operation(summary = "Add a travel expense")
    @PostMapping("/{requestId}")
    public ResponseEntity<TravelExpenseDTO> addExpense(
            @Parameter(description = "Travel request ID") @PathVariable UUID requestId,
            @Valid @RequestBody TravelExpenseDTO dto) {
        return ResponseEntity.ok(service.addExpense(requestId, dto));
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{expenseId}")
    public ResponseEntity<TravelExpenseDTO> getExpense(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId) {
        return ResponseEntity.ok(service.getExpense(expenseId));
    }

    @Operation(summary = "List expenses for a request")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<List<TravelExpenseDTO>> getByRequest(
            @Parameter(description = "Travel request ID") @PathVariable UUID requestId) {
        return ResponseEntity.ok(service.getExpensesForRequest(requestId));
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId) {
        service.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
