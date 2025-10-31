package com.bwc.employee_management_service.repository;

import com.bwc.employee_management_service.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository 
        extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartment(String department);

    List<Employee> findByIsActiveTrue();

    List<Employee> findByManagerEmployeeId(UUID managerId);

    @Query("SELECT e FROM Employee e WHERE e.fullName LIKE %:name%")
    List<Employee> findByFullNameContaining(@Param("name") String name);

    @Query("SELECT e FROM Employee e WHERE e.fullName LIKE %:name% AND e.department = :department")
    Page<Employee> findByFullNameContainingAndDepartment(@Param("name") String name,
                                                        @Param("department") String department,
                                                        Pageable pageable);

    boolean existsByEmail(String email);

    Page<Employee> findByIsActive(Boolean isActive, Pageable pageable);

    // ✅ New query to fetch employees by role name
    @Query("SELECT e FROM Employee e JOIN e.roles r WHERE r.roleName = :roleName")
    List<Employee> findByRoleName(@Param("roleName") String roleName);
}
