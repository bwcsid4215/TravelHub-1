package com.bwc.authservice.controller;

import com.bwc.authservice.dto.AuthRequest;
import com.bwc.authservice.dto.AuthResponse;
import com.bwc.authservice.dto.UserRegistrationDTO;
import com.bwc.authservice.entity.AuthUser;
import com.bwc.authservice.service.AuthService;
import com.bwc.authservice.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest req, HttpServletResponse response) {
        AuthResponse resp = service.login(req);

        // ✅ Cookie correctly scoped to BWC-97.brainwaveconsulting.co.in
        ResponseCookie cookie = ResponseCookie.from("auth_token", resp.getAccessToken())
        	    .httpOnly(true)
        	    .secure(false)
        	    .sameSite("Lax")
        	    .domain(".brainwaveconsulting.co.in")
        	    .path("/")
        	    .maxAge(resp.getExpiresIn())
        	    .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Parse token to get user info
        var claims = jwtUtil.parseToken(resp.getAccessToken());
        var roles = (List<String>) claims.getClaim("roles");
        String role = (roles != null && !roles.isEmpty()) ? roles.get(0) : "UNKNOWN";

        System.out.println("✅ Cookie sent for domain: BWC-97.brainwaveconsulting.co.in");
        System.out.println("✅ Set-Cookie Header: " + cookie.toString());

        return ResponseEntity.ok(Map.of("role", role));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUser> register(@RequestBody UserRegistrationDTO dto) {
        return ResponseEntity.ok(service.register(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String,Object>> me(@CookieValue(name="auth_token", required=false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            var claims = jwtUtil.parseToken(token);
            var roles = (List<String>) claims.getClaim("roles");
            Map<String,Object> user = Map.of(
                "userId", claims.getSubject(),
                "email", claims.getStringClaim("email"),
                "department", claims.getStringClaim("department"),
                "roles", roles
            );
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
    	ResponseCookie cookie = ResponseCookie.from("auth_token", "")
    	        .httpOnly(true)
    	        .secure(false)
    	        .sameSite("Lax")
    	        .domain(".brainwaveconsulting.co.in")  // 👈 ADD THIS
    	        .path("/")
    	        .maxAge(0)
    	        .build();


        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        System.out.println("🚪 Logout cookie cleared for .brainwaveconsulting.co.in");
        return ResponseEntity.noContent().build();
    }
}
