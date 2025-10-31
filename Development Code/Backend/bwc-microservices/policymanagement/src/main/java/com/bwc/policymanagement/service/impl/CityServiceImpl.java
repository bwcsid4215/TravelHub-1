package com.bwc.policymanagement.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bwc.common.exception.BusinessException;
import com.bwc.common.exception.ResourceNotFoundException;
import com.bwc.policymanagement.dto.CityRequest;
import com.bwc.policymanagement.dto.CityResponse;
import com.bwc.policymanagement.entity.City;
import com.bwc.policymanagement.entity.CityCategory;
import com.bwc.policymanagement.mapper.CityMapper;
import com.bwc.policymanagement.repository.CityCategoryRepository;
import com.bwc.policymanagement.repository.CityRepository;
import com.bwc.policymanagement.service.CityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityCategoryRepository categoryRepository;
    private final CityMapper cityMapper;

    @Override
    @CacheEvict(value = {"cities", "citiesByCategory"}, allEntries = true)
    public CityResponse createCity(CityRequest request) {
        log.info("Creating city: {} for category ID: {}", request.getName(), request.getCategoryId());

        CityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", request.getCategoryId().toString()));

        if (cityRepository.existsByNameIgnoreCaseAndCategoryId(request.getName(), request.getCategoryId())) {
            throw new BusinessException("City with name '" + request.getName() + "' already exists in this category");
        }

        City city = City.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .build();

        City savedCity = cityRepository.save(city);
        log.info("Successfully created city: {} with ID: {}", request.getName(), savedCity.getId());

        return cityMapper.toDto(savedCity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("cities")
    public List<CityResponse> getAllCities() {
        log.info("Fetching all cities");
        List<City> cities = cityRepository.findAll();
        return cityMapper.toDtoList(cities);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cities", key = "#id")
    public CityResponse getCityById(UUID id) {
        log.info("Fetching city by ID: {}", id);
        City city = cityRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id.toString()));
        return cityMapper.toDto(city);
    }

    @Override
    @CacheEvict(value = {"cities", "citiesByCategory"}, allEntries = true)
    public CityResponse updateCity(UUID id, CityRequest request) {
        log.info("Updating city with ID: {}", id);

        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id.toString()));

        CityCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CityCategory", request.getCategoryId().toString()));

        if (!existingCity.getName().equalsIgnoreCase(request.getName()) &&
                cityRepository.existsByNameIgnoreCaseAndCategoryId(request.getName(), request.getCategoryId())) {
            throw new BusinessException("City with name '" + request.getName() + "' already exists in this category");
        }

        existingCity.setName(request.getName());
        existingCity.setDescription(request.getDescription());
        existingCity.setCategory(category);

        City updatedCity = cityRepository.save(existingCity);
        log.info("Successfully updated city with ID: {}", id);

        return cityMapper.toDto(updatedCity);
    }

    @Override
    @CacheEvict(value = {"cities", "citiesByCategory"}, allEntries = true)
    public void deleteCity(UUID id) {
        log.info("Deleting city with ID: {}", id);

        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id.toString()));

        cityRepository.delete(city);
        log.info("Successfully deleted city with ID: {}", id);
    }
}
