// src/main/java/com/bwc/policymanagement/repository/CityCategoryRepository.java
package com.bwc.policymanagement.repository;

import com.bwc.policymanagement.entity.CityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityCategoryRepository extends JpaRepository<CityCategory, UUID> {
    
    Optional<CityCategory> findByName(String name);
    
    Optional<CityCategory> findByNameIgnoreCase(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT cc FROM CityCategory cc LEFT JOIN FETCH cc.cities WHERE cc.id = :id")
    Optional<CityCategory> findByIdWithCities(@Param("id") UUID id);
    
    @Query("SELECT cc FROM CityCategory cc LEFT JOIN FETCH cc.policies WHERE cc.id = :id")
    Optional<CityCategory> findByIdWithPolicies(@Param("id") UUID id);
}