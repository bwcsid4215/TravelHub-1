// src/main/java/com/bwc/policymanagement/repository/GradePolicyRepository.java
package com.bwc.policymanagement.repository;

import com.bwc.policymanagement.entity.GradePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradePolicyRepository extends JpaRepository<GradePolicy, UUID> {
    
    @Query("SELECT gp FROM GradePolicy gp " +
           "JOIN gp.policy p " +
           "JOIN p.category cc " +
           "JOIN cc.cities c " +
           "WHERE c.name = :cityName AND gp.grade = :grade AND p.active = true")
    Optional<GradePolicy> findByCityNameAndGradeAndActivePolicy(
            @Param("cityName") String cityName, 
            @Param("grade") String grade);
    
    boolean existsByPolicyIdAndGrade(UUID policyId, String grade);
    
    @Query("SELECT gp FROM GradePolicy gp " +
           "LEFT JOIN FETCH gp.travelModes tm " +
           "LEFT JOIN FETCH tm.allowedClasses " +
           "WHERE gp.id = :id")
    Optional<GradePolicy> findByIdWithDetails(@Param("id") UUID id);
}