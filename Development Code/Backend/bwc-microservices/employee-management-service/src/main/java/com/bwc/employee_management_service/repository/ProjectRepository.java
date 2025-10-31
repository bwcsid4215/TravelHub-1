package com.bwc.employee_management_service.repository;

import com.bwc.employee_management_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByProjectName(String projectName);
    
    @Query("SELECT p FROM Project p WHERE p.projectName LIKE %:name%")
    List<Project> findByProjectNameContaining(@Param("name") String name);
    
    boolean existsByProjectName(String projectName);
}

