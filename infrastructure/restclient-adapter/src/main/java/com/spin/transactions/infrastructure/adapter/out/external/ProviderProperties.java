package com.spin.transactions.infrastructure.adapter.out.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration for the provider base URL and HTTP timeout.
 *
 * @param url provider service base URL
 * @param timeout connection and response timeout
 */
@ConfigurationProperties(prefix = "external.provider")
public record ProviderProperties(String url, Duration timeout) {
    /** Applies the default timeout when configuration omits one. */
    public ProviderProperties {
        timeout = timeout == null ? Duration.ofSeconds(3) : timeout;
    }
}
