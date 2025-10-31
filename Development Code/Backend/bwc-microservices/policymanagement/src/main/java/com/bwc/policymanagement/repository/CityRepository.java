// src/main/java/com/bwc/policymanagement/repository/CityRepository.java
package com.bwc.policymanagement.repository;

import com.bwc.policymanagement.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    
    Optional<City> findByName(String name);
    
    Optional<City> findByNameIgnoreCase(String name);
    
    boolean existsByNameAndCategoryId(String name, UUID categoryId);
    
    boolean existsByNameIgnoreCaseAndCategoryId(String name, UUID categoryId);
    
    List<City> findByCategoryId(UUID categoryId);
    
    @Query("SELECT c FROM City c LEFT JOIN FETCH c.category WHERE c.name = :name")
    Optional<City> findByNameWithCategory(@Param("name") String name);
    
    @Query("SELECT c FROM City c LEFT JOIN FETCH c.category WHERE c.id = :id")
    Optional<City> findByIdWithCategory(@Param("id") UUID id);
}