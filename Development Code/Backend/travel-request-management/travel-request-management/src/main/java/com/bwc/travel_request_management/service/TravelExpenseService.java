package com.bwc.travel_request_management.service;

import com.bwc.travel_request_management.dto.TravelExpenseDTO;

import java.util.List;
import java.util.UUID;

public interface TravelExpenseService {
    TravelExpenseDTO addExpense(UUID requestId, TravelExpenseDTO expenseDto);
    TravelExpenseDTO getExpense(UUID expenseId);
    List<TravelExpenseDTO> getExpensesForRequest(UUID requestId);
    void deleteExpense(UUID expenseId);
}
