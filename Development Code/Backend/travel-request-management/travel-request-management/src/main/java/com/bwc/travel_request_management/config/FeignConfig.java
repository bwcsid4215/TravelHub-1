package com.bwc.travel_request_management.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ✅ Adds required headers for internal secure communication between microservices.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor internalGatewayHeaderInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-Gateway-Secret", "bwc-secure-gateway"); // must match gateway + workflow filter
            System.out.println("🟢 [TRMS → Workflow] Added X-Internal-Gateway-Secret header");
        };
    }
}
