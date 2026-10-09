package com.spin.transactions.infrastructure.adapter.out.external;

import com.spin.transactions.domain.exception.ProviderUnavailableException;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import com.spin.transactions.domain.model.ProviderTransactionOutcome;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ExternalProviderRestClientAdapterTest {

    @Test
    void sendsIdempotencyKeyAndMapsApprovedResponse() {
        final TestClient client = testClient();
        client.server().expect(requestTo("http://provider.test/provider/v1/execute"))
                .andExpect(method(POST))
                .andExpect(header("Idempotency-Key", "request-key"))
                .andRespond(withSuccess("""
                        {
                          "transactionId": "provider-123",
                          "status": "APPROVED",
                          "balance": 5500.00,
                          "executedAt": "2025-03-15T10:30:00Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        ProviderTransactionResult result = client.adapter().processTransaction(transaction(), "request-key");

        assertEquals(ProviderTransactionOutcome.APPROVED, result.outcome());
        assertEquals("provider-123", result.providerTransactionId());
        assertEquals(new BigDecimal("5500.00"), result.balanceAfter());
        assertEquals(Instant.parse("2025-03-15T10:30:00Z"), result.executedAt());
        client.server().verify();
    }

    @Test
    void mapsProviderBusinessRejection() {
        TestClient client = testClient();
        client.server().expect(requestTo("http://provider.test/provider/v1/execute"))
                .andRespond(withStatus(UNPROCESSABLE_CONTENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "status": "REJECTED",
                                  "code": "INSUFFICIENT_FUNDS",
                                  "message": "The account does not have enough balance"
                                }
                                """));

        ProviderTransactionResult result = client.adapter().processTransaction(transaction(), "request-key");

        assertEquals(ProviderTransactionOutcome.REJECTED, result.outcome());
        assertEquals("INSUFFICIENT_FUNDS", result.errorCode());
        assertEquals("The account does not have enough balance", result.errorMessage());
        client.server().verify();
    }

    @Test
    void mapsServerFailureToUnavailableProviderError() {
        TestClient client = testClient();
        client.server().expect(requestTo("http://provider.test/provider/v1/execute"))
                .andRespond(withServerError());

        ProviderUnavailableException error = assertThrows(
                ProviderUnavailableException.class,
                () -> client.adapter().processTransaction(transaction(), "request-key"));

        assertEquals("PROVIDER_ERROR", error.code());
        client.server().verify();
    }

    @Test
    void mapsMalformedSuccessfulResponseToUnavailableProviderError() {
        TestClient client = testClient();
        client.server().expect(requestTo("http://provider.test/provider/v1/execute"))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

        ProviderUnavailableException error = assertThrows(
                ProviderUnavailableException.class,
                () -> client.adapter().processTransaction(transaction(), "request-key"));

        assertEquals("INVALID_PROVIDER_RESPONSE", error.code());
        client.server().verify();
    }

    @Test
    void doesNotRetryProviderCalls() {
        TestClient client = testClient();
        client.server().expect(requestTo("http://provider.test/provider/v1/execute"))
                .andExpect(method(POST))
                .andRespond(withServerError());

        assertThrows(ProviderUnavailableException.class,
                () -> client.adapter().processTransaction(transaction(), "request-key"));
        client.server().verify();
    }

    @Test
    void mapsProviderTimeoutToUnavailableError() {
        ClientHttpRequestFactory requestFactory = (URI uri, HttpMethod method) -> {
            throw new org.springframework.web.client.ResourceAccessException(
                    "Provider request timed out", new HttpTimeoutException("timeout"));
        };
        ExternalProviderRestClientAdapter adapter = new ExternalProviderRestClientAdapter(
                new ExternalProviderRestClient(RestClient.builder()
                        .baseUrl("http://provider.test")
                        .requestFactory(requestFactory)
                        .build()),
                CircuitBreaker.of("provider-timeout-test", CircuitBreakerConfig.ofDefaults()));

        ProviderUnavailableException error = assertThrows(
                ProviderUnavailableException.class,
                () -> adapter.processTransaction(transaction(), "request-key"));

        assertEquals("PROVIDER_TIMEOUT", error.code());
    }

    private TestClient testClient() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://provider.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ExternalProviderRestClient externalProviderRestClient =
                new ExternalProviderRestClient(builder.build());
        ExternalProviderRestClientAdapter adapter = new ExternalProviderRestClientAdapter(
                externalProviderRestClient, CircuitBreaker.of("provider-test", CircuitBreakerConfig.ofDefaults()));
        return new TestClient(server, adapter);
    }

    private Transaction transaction() {
        return TransactionFixtures.aPendingCreditTransaction();
    }

}
