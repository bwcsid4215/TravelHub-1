package com.bwc.policymanagement.service.impl;

import com.bwc.common.exception.BusinessException;
import com.bwc.common.exception.ResourceNotFoundException;
import com.bwc.policymanagement.dto.CityCategoryRequest;
import com.bwc.policymanagement.dto.CityCategoryResponse;
import com.bwc.policymanagement.entity.CityCategory;
import com.bwc.policymanagement.mapper.CityCategoryMapper;
import com.bwc.policymanagement.repository.CityCategoryRepository;
import com.bwc.policymanagement.service.CityCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CityCategoryServiceImpl implements CityCategoryService {

    private final CityCategoryRepository categoryRepository;
    private final CityCategoryMapper categoryMapper;

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CityCategoryResponse createCategory(CityCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("City category with name '" + request.getName() + "' already exists");
        }

        CityCategory category = CityCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("categories")
    public List<CityCategoryResponse> getAllCategories() {
        return categoryMapper.toDtoList(categoryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id")
    public CityCategoryResponse getCategoryById(UUID id) {
        CityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", id.toString()));
        return categoryMapper.toDto(category);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CityCategoryResponse updateCategory(UUID id, CityCategoryRequest request) {
        CityCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", id.toString()));

        if (!existing.getName().equalsIgnoreCase(request.getName()) &&
            categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("City category with name '" + request.getName() + "' already exists");
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        return categoryMapper.toDto(categoryRepository.save(existing));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(UUID id) {
        CityCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", id.toString()));

        if (!category.getCities().isEmpty()) {
            throw new BusinessException("Cannot delete category with associated cities");
        }

        if (!category.getPolicies().isEmpty()) {
            throw new BusinessException("Cannot delete category with associated policies");
        }

        categoryRepository.delete(category);
    }
}
