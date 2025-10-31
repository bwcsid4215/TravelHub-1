package com.bwc.approval_workflow_service.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class GatewaySecurityFilter implements Filter {

    @Value("${gateway.internal.secret}")
    private String expectedGatewaySecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();
        
        // 🟢 Skip gateway validation for public endpoints
        if (isPublicEndpoint(path)) {
            System.out.println("🔐 [Workflow] Skipping gateway validation for public endpoint: " + path);
            chain.doFilter(request, response);
            return;
        }

        String headerSecret = httpReq.getHeader("X-Internal-Gateway-Secret");

        // 🟢 DEBUG
        System.out.println("🔐 [Workflow] Path: " + path);
        System.out.println("🔐 [Workflow] Expected Secret: " + expectedGatewaySecret);
        System.out.println("🔐 [Workflow] Received Header Secret: " + headerSecret);

        if (headerSecret == null || !headerSecret.equals(expectedGatewaySecret)) {
            System.out.println("❌ [Workflow] Forbidden — Secret mismatch or missing header!");
            httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpRes.setContentType("application/json");
            httpRes.getWriter().write("{\"error\": \"Forbidden: Request must originate from API Gateway\"}");
            return;
        }

        System.out.println("✅ [Workflow] Gateway secret validated successfully.");
        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") || 
               path.startsWith("/api-docs") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/swagger-resources");
    }
}