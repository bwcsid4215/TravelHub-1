package com.bwc.policymanagement.service;

import com.bwc.policymanagement.dto.CityCategoryRequest;
import com.bwc.policymanagement.dto.CityCategoryResponse;
import java.util.List;
import java.util.UUID;

public interface CityCategoryService {
    CityCategoryResponse createCategory(CityCategoryRequest request);
    List<CityCategoryResponse> getAllCategories();
    CityCategoryResponse getCategoryById(UUID id);
    CityCategoryResponse updateCategory(UUID id, CityCategoryRequest request);
    void deleteCategory(UUID id);
}
