// src/main/java/com/bwc/authservice/entity/AuthUser.java
package com.bwc.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "auth_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthUser {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    // store BCrypt hash
    @Column(nullable = false)
    private String passwordHash;

    // link to employee service identity (UUID as string)
    @Column(nullable = false, unique = true)
    private String employeeId;
}
