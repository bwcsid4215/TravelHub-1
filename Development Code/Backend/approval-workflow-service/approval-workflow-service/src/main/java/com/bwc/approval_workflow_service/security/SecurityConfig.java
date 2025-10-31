package com.bwc.approval_workflow_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final GatewaySecurityFilter gatewaySecurityFilter;
    private final GatewayAuthHeaderVerifier gatewayAuthHeaderVerifier;

    public SecurityConfig(GatewaySecurityFilter gatewaySecurityFilter,
                          GatewayAuthHeaderVerifier gatewayAuthHeaderVerifier) {
        this.gatewaySecurityFilter = gatewaySecurityFilter;
        this.gatewayAuthHeaderVerifier = gatewayAuthHeaderVerifier;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 🟢 Public endpoints (no auth)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/management/**",
                    // ✅ Allow both initiation endpoints (for internal service calls)
                    "/api/workflows/initiate",
                    "/api/workflows/initiate-with-travel-request"
                ).permitAll()

                // 🟢 Role-based endpoints
                .requestMatchers("/api/manager/**").hasRole("MANAGER")
                .requestMatchers("/api/finance/**").hasRole("FINANCE")
                .requestMatchers("/api/hr/**").hasRole("HR")
                .requestMatchers("/api/travel-desk/**").hasRole("TRAVEL_DESK")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 🟡 Authenticated workflows
                .requestMatchers("/api/workflows/**").authenticated()

                // 🔒 Everything else requires auth
                .anyRequest().authenticated()
            )
            // 🧱 Filters order: Gateway → Auth Header → Rest
            .addFilterBefore(gatewaySecurityFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(gatewayAuthHeaderVerifier, GatewaySecurityFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
