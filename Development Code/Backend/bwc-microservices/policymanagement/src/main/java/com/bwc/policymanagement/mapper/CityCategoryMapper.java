package com.bwc.policymanagement.mapper;

import com.bwc.policymanagement.dto.CityCategoryResponse;
import com.bwc.policymanagement.entity.CityCategory;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CityCategoryMapper {
    CityCategoryResponse toDto(CityCategory category);
    List<CityCategoryResponse> toDtoList(List<CityCategory> categories);
}
