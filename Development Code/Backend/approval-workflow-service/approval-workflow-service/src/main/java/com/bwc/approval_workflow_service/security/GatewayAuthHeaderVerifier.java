package com.bwc.approval_workflow_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(2)
public class GatewayAuthHeaderVerifier extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // 🟢 Skip authentication setup for public endpoints
        if (isPublicEndpoint(path)) {
            System.out.println("🟩 [Workflow] Skipping auth setup for public endpoint: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // 🟢 Allow workflow initiation without user context (internal service call)
        if (path.equals("/api/workflows/initiate")) {
            System.out.println("🟩 [Workflow] Internal service call to initiate workflow - allowing without user context");
            filterChain.doFilter(request, response);
            return;
        }

        String rolesHeader = request.getHeader("X-User-Roles");
        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-User-Email");

        // 🟢 DEBUG LOGS
        System.out.println("🟩 [Workflow] Path: " + path);
        System.out.println("🟩 [Workflow] Received X-User-Id: " + userId);
        System.out.println("🟩 [Workflow] Received X-User-Email: " + userEmail);
        System.out.println("🟩 [Workflow] Received X-User-Roles: " + rolesHeader);

        // For internal service calls (no user context), proceed without authentication
        if (userId == null && rolesHeader == null) {
            System.out.println("🟡 [Workflow] No user context - internal service call");
            filterChain.doFilter(request, response);
            return;
        }

        // If we have user info but missing roles, treat as unauthenticated
        if (userId != null && rolesHeader == null) {
            System.out.println("⚠️ [Workflow] User ID present but no roles - treating as unauthenticated");
            filterChain.doFilter(request, response);
            return;
        }

        if (userId != null && rolesHeader != null) {
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            // Create authentication token with principal as user ID
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("✅ [Workflow] Security context set for user: " + userId + " with authorities: " + authorities);
        } else {
            System.out.println("⚠️ [Workflow] No authentication set - proceeding without security context");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") || 
               path.startsWith("/api-docs") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/management/") ||
               path.equals("/api/workflows/initiate"); // Add workflow initiation as public
    }
}