package com.spin.transactions.infrastructure.config;

import com.spin.transactions.infrastructure.adapter.out.external.ProviderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/**
 * Builds the HTTP client used for outbound provider requests.
 */
@Configuration
@EnableConfigurationProperties(ProviderProperties.class)
public class RestClientConfig {

    /** Creates the provider HTTP client configuration. */
    public RestClientConfig() {
    }

    /**
     * Creates a provider client with configured base URL and connection/read timeouts.
     *
     * @param properties provider URL and timeout configuration
     * @return configured Spring HTTP client
     */
    @Bean
    public RestClient providerRestClient(ProviderProperties properties) {
        final var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.timeout())
                .build();
        final var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.url())
                .requestFactory(requestFactory)
                .build();
    }
}
