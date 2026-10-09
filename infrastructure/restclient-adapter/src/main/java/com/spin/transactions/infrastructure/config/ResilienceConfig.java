package com.spin.transactions.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configures provider circuit-breaker thresholds and registry.
 */
@Configuration
public class ResilienceConfig {

    /** Creates the resilience configuration. */
    public ResilienceConfig() {
    }

    /**
     * Creates the registry used to share configured circuit breakers.
     *
     * @return registry configured with bounded failure and recovery windows
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        final CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    /**
     * Creates the named circuit breaker used by the provider adapter.
     *
     * @param registry circuit breaker registry
     * @return provider circuit breaker
     */
    @Bean
    public CircuitBreaker externalProviderCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("externalProvider");
    }
}
