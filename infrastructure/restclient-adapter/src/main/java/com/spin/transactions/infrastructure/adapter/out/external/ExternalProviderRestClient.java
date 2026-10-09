package com.spin.transactions.infrastructure.adapter.out.external;

import com.spin.transactions.infrastructure.adapter.out.external.dto.ProviderExecutionRequest;
import com.spin.transactions.infrastructure.adapter.out.external.dto.ProviderExecutionResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Transport-specific client that submits execution requests to the external provider.
 *
 * <p>HTTP request construction is kept out of the application-facing adapter.
 */
@Component
public class ExternalProviderRestClient {

    private final RestClient restClient;

    /**
     * Creates the provider client around the configured Spring HTTP client.
     *
     * @param providerRestClient configured client with provider base URL and timeouts
     */
    public ExternalProviderRestClient(RestClient providerRestClient) {
        this.restClient = providerRestClient;
    }

    /**
     * Sends an execution request to the provider.
     *
     * @param request provider contract payload
     * @param idempotencyKey key forwarded for provider-side duplicate protection
     * @return provider response, or {@code null} when the response body is empty
     */
    public ProviderExecutionResponse execute(ProviderExecutionRequest request, String idempotencyKey) {
        return this.restClient.post()
                .uri("/provider/v1/execute")
                .header("Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .body(ProviderExecutionResponse.class);
    }
}
