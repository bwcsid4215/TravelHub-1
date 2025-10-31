package com.bwc.gateway.filter;

import com.bwc.gateway.security.JwtGatewayUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtGatewayUtil jwtUtil;

    @Value("${gateway.internal.secret:bwc-secure-gateway}")
    private String internalSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // If header missing, check cookie
        if (auth == null || !auth.startsWith("Bearer ")) {
            List<String> cookieHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.COOKIE);
            if (cookieHeaders != null) {
                for (String cookieHeader : cookieHeaders) {
                    for (String cookiePair : cookieHeader.split(";")) {
                        String trimmed = cookiePair.trim();
                        if (trimmed.startsWith("auth_token=")) {
                            String cookieToken = trimmed.substring("auth_token=".length());
                            auth = "Bearer " + cookieToken;
                            break;
                        }
                    }
                    if (auth != null) break;
                }
            }
        }

        if (auth == null || !auth.startsWith("Bearer ")) {
            System.out.println("⚪ [Gateway] No token for path: " + path);
            return chain.filter(exchange);
        }

        String token = auth.substring(7);
        try {
            JWTClaimsSet claims = jwtUtil.parseToken(token);

            String userId = claims.getSubject();
            List<String> roles = claims.getStringListClaim("roles");
            String email = claims.getStringClaim("email");

            if (path.startsWith("/api/manager/") && (roles == null || !roles.contains("MANAGER"))) {
                System.out.println("🔴 [Gateway] Access denied for non-manager");
                return chain.filter(exchange);
            }

            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.add("X-User-Id", userId);
                        headers.add("X-Internal-Gateway-Secret", internalSecret);
                        if (email != null) headers.add("X-User-Email", email);
                        if (roles != null && !roles.isEmpty()) {
                            headers.add("X-User-Roles", String.join(",", roles));
                        }
                    }))
                    .build();

            return chain.filter(mutated);

        } catch (Exception e) {
            System.out.println("❌ [Gateway] Token parse failed: " + e.getMessage());
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
