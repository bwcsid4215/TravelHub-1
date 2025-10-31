package com.bwc.authservice.service;

import com.bwc.authservice.client.EmployeeServiceClient;
import com.bwc.authservice.client.dto.EmployeeProxyDTO;
import com.bwc.authservice.dto.AuthRequest;
import com.bwc.authservice.dto.AuthResponse;
import com.bwc.authservice.dto.UserRegistrationDTO;
import com.bwc.authservice.entity.AuthUser;
import com.bwc.authservice.repository.AuthUserRepository;
import com.bwc.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.*;
import com.nimbusds.jwt.JWTClaimsSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository repo;
    private final JwtUtil jwtUtil;
    private final EmployeeServiceClient employeeClient;

    public AuthResponse login(AuthRequest req) {
        AuthUser user = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        UUID empId = UUID.fromString(user.getEmployeeId());
        EmployeeProxyDTO emp = employeeClient.getEmployee(empId);

        Set<String> roles = emp != null && emp.getRoles() != null ? emp.getRoles() : Set.of();
        String department = emp != null ? emp.getDepartment() : "Unknown";

        // 🟢 DEBUG LOG
        System.out.println("🟢 [AuthService] Roles fetched from EmployeeService: " + roles);

        // 🔐 Token includes roles
        String token = jwtUtil.generateToken(user.getEmployeeId(), user.getEmail(), department, roles);

        // 🧩 DEBUG: Decode back to verify token content
        var parsed = jwtUtil.parseToken(token);
        System.out.println("🟢 [AuthService] JWT Payload after encryption: " + parsed.toJSONObject());

        // Expiry info
        Date expiration = parsed.getExpirationTime();
        long now = System.currentTimeMillis();
        long expiresInSeconds = (expiration.getTime() - now) / 1000;

        return AuthResponse.builder()
                .accessToken(token)
                .expiresIn((int) expiresInSeconds)
                .build();
    }


    public AuthUser register(UserRegistrationDTO dto) {
        if (repo.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (repo.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
            throw new IllegalArgumentException("Employee already registered");
        }

        String hash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        AuthUser user = AuthUser.builder()
                .email(dto.getEmail())
                .passwordHash(hash)
                .employeeId(dto.getEmployeeId())
                .build();

        return repo.save(user);
    }
}
