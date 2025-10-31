package com.bwc.policymanagement.repository;

import com.bwc.policymanagement.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    
    List<Policy> findByCategoryId(UUID categoryId);
    
    Optional<Policy> findByCategoryIdAndYear(UUID categoryId, Integer year);
    
    @Query("SELECT p FROM Policy p WHERE p.category.id = :categoryId AND p.active = true")
    Optional<Policy> findByCategoryIdAndActiveTrue(@Param("categoryId") UUID categoryId);
    
    List<Policy> findByActiveTrue();
    
    @Query("SELECT p FROM Policy p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Policy> findByIdWithCategory(@Param("id") UUID id);
    
    @Query("SELECT p FROM Policy p " +
           "LEFT JOIN FETCH p.gradePolicies gp " +
           "LEFT JOIN FETCH gp.travelModes tm " +
           "LEFT JOIN FETCH tm.allowedClasses " +
           "WHERE p.id = :id")
    Optional<Policy> findByIdWithDetails(@Param("id") UUID id);
}