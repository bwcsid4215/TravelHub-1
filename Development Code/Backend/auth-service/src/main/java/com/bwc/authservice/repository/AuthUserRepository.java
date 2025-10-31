// src/main/java/com/bwc/authservice/repository/AuthUserRepository.java
package com.bwc.authservice.repository;

import com.bwc.authservice.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByEmail(String email);
    Optional<AuthUser> findByEmployeeId(String employeeId);
}
