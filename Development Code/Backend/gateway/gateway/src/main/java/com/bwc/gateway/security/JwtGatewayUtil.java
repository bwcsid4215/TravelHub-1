package com.bwc.gateway.security;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class JwtGatewayUtil {

    private final byte[] aesKey;

    public JwtGatewayUtil(@Value("${jwt.secret}") String secret) {
        this.aesKey = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 32);
    }

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
