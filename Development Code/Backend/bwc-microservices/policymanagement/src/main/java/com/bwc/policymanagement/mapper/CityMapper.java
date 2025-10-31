package com.bwc.policymanagement.mapper;

import com.bwc.policymanagement.dto.CityResponse;
import com.bwc.policymanagement.dto.CityCategoryResponse;
import com.bwc.policymanagement.entity.City;
import com.bwc.policymanagement.entity.CityCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CityMapper {

    CityMapper INSTANCE = Mappers.getMapper(CityMapper.class);

    @Mapping(source = "category", target = "category")
    CityResponse toDto(City city);

    List<CityResponse> toDtoList(List<City> cities);

    CityCategoryResponse toDto(CityCategory category);
}
