package com.bwc.authservice.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtUtil {

    private final byte[] aesKey; // 32 bytes = AES-256
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.aesKey = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 32);
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Generate AES-encrypted JWT (JWE) with extra info inside the payload
     */
    public String generateToken(String userId, String email, String department, Collection<String> roles) {
        try {
            Date now = new Date();
            Date exp = new Date(now.getTime() + expirationSeconds * 1000L);

            // 1️⃣ Create JWT claims (payload)
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .subject(userId)
                    .claim("email", email)
                    .claim("department", department)
                    .claim("roles", roles) // stored as array, not string
                    .issueTime(now)
                    .expirationTime(exp)
                    .build();

            // 2️⃣ Header for AES-256-GCM encryption
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                    .contentType("JWT")
                    .build();

            // 3️⃣ Encrypt the JWT
            EncryptedJWT jwt = new EncryptedJWT(header, claims);
            jwt.encrypt(new DirectEncrypter(aesKey));

            // 4️⃣ Return token
            return jwt.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encrypted token", e);
        }
    }

    /**
     * Decrypt and parse AES-encrypted JWT (JWE)
     */
    public JWTClaimsSet parseToken(String token) {
        try {
            EncryptedJWT jwt = EncryptedJWT.parse(token);
            jwt.decrypt(new DirectDecrypter(aesKey));
            return jwt.getJWTClaimsSet();
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token", e);
        }
    }
}
