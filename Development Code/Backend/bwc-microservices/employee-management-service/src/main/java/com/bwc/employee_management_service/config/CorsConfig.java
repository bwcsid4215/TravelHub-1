package com.bwc.employee_management_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origin-patterns:http://localhost:3000,http://localhost:3001}")
    private String[] allowedOriginPatterns;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**")
                    .allowedOriginPatterns(allowedOriginPatterns)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);

                // Allow API docs and Swagger UI
                registry.addMapping("/v3/api-docs/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET")
                    .allowedHeaders("*")
                    .maxAge(3600);

                registry.addMapping("/swagger-ui/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET")
                    .allowedHeaders("*")
                    .maxAge(3600);
            }
        };
    }
}
