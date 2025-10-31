//// common-lib/src/main/java/com/bwc/common/config/ResilienceConfig.java
//package com.bwc.common.config;
//
//import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
//import io.github.resilience4j.retry.RetryConfig;
//import io.github.resilience4j.timelimiter.TimeLimiterConfig;
//import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
//import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
//import org.springframework.cloud.client.circuitbreaker.Customizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.bwc.common.exception.BusinessException;
//
//import java.time.Duration;
//
//@Configuration
//public class ResilienceConfig {
//
//    @Bean
//    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
//        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                .timeLimiterConfig(TimeLimiterConfig.custom()
//                        .timeoutDuration(Duration.ofSeconds(5))
//                        .build())
//                .circuitBreakerConfig(CircuitBreakerConfig.custom()
//                        .slidingWindowSize(10)
//                        .failureRateThreshold(50)
//                        .waitDurationInOpenState(Duration.ofSeconds(10))
//                        .permittedNumberOfCallsInHalfOpenState(5)
//                        .build())
//                .build());
//    }
//
//    @Bean
//    public RetryConfig retryConfig() {
//        return RetryConfig.custom()
//                .maxAttempts(3)
//                .waitDuration(Duration.ofMillis(500))
//                .retryOnException(e -> !(e instanceof BusinessException))
//                .build();
//    }
//}
