package com.bwc.policymanagement.service;

import com.bwc.policymanagement.dto.CityRequest;
import com.bwc.policymanagement.dto.CityResponse;

import java.util.List;
import java.util.UUID;

public interface CityService {
    CityResponse createCity(CityRequest request);
    List<CityResponse> getAllCities();
    CityResponse getCityById(UUID id);
    CityResponse updateCity(UUID id, CityRequest request);
    void deleteCity(UUID id);
}
