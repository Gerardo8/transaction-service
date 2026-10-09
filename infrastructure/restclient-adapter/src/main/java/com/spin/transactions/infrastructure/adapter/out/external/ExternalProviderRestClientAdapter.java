package com.spin.transactions.infrastructure.adapter.out.external;

import com.spin.transactions.application.port.out.ExternalProviderPort;
import com.spin.transactions.domain.exception.ProviderUnavailableException;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Adapts provider HTTP responses and transport failures to the application port's domain result.
 */
@Component
public class ExternalProviderRestClientAdapter implements ExternalProviderPort {

    private final ExternalProviderRestClient externalProviderRestClient;
    private final CircuitBreaker circuitBreaker;

    /**
     * Creates the provider adapter with an HTTP wrapper and circuit breaker.
     *
     * @param externalProviderRestClient provider HTTP client
     * @param externalProviderCircuitBreaker circuit breaker protecting provider calls
     */
    public ExternalProviderRestClientAdapter(
            ExternalProviderRestClient externalProviderRestClient,
            CircuitBreaker externalProviderCircuitBreaker) {
        this.externalProviderRestClient = externalProviderRestClient;
        this.circuitBreaker = externalProviderCircuitBreaker;
    }

    /**
     * Executes one provider call and normalizes approvals, rejections, and transport failures.
     *
     * @param transaction transaction to send
     * @param idempotencyKey stable request key forwarded to the provider
     * @return normalized provider result
     * @throws ProviderUnavailableException when provider availability or response prevents execution
     */
    @Override
    public ProviderTransactionResult processTransaction(Transaction transaction, String idempotencyKey) {
        try {
            return this.circuitBreaker.executeSupplier(() -> this.executeProviderRequest(transaction, idempotencyKey));
        } catch (CallNotPermittedException error) {
            throw new ProviderUnavailableException("PROVIDER_UNAVAILABLE", "Provider circuit breaker is open", error);
        }
    }

    private ProviderTransactionResult executeProviderRequest(Transaction transaction, String idempotencyKey) {
        try {
            final var response = this.externalProviderRestClient.execute(
                    ProviderTransactionMapper.toRequest(transaction), idempotencyKey);
            if (response == null) {
                throw this.invalidProviderResponse();
            }
            return ProviderTransactionMapper.toResult(response);

        } catch (HttpClientErrorException error) {
            final var providerError = ProviderTransactionMapper.toError(error);

            return ProviderTransactionResult.rejected(
                    null,
                    null,
                    null,
                    providerError.code() == null ? "PROVIDER_REJECTED" : providerError.code(),
                    providerError.message() == null
                            ? "Provider rejected the transaction with HTTP " + error.getStatusCode().value()
                            : providerError.message());

        } catch (HttpServerErrorException error) {
            throw new ProviderUnavailableException(
                    "PROVIDER_ERROR",
                    "Provider returned HTTP " + error.getStatusCode().value(),
                    error
            );
        } catch (ResourceAccessException error) {

            final String code = this.isTimeout(error) ? "PROVIDER_TIMEOUT" : "PROVIDER_UNAVAILABLE";
            final String message = this.isTimeout(error) ? "Provider execution timed out" : "Provider could not be reached";

            throw new ProviderUnavailableException(code, message, error);

        } catch (HttpMessageNotReadableException | RestClientException error) {

            throw new ProviderUnavailableException("INVALID_PROVIDER_RESPONSE", "Provider returned an invalid response", error);
        }
    }

    private boolean isTimeout(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.net.http.HttpTimeoutException
                    || cause instanceof java.net.SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private ProviderUnavailableException invalidProviderResponse() {
        return new ProviderUnavailableException("INVALID_PROVIDER_RESPONSE", "Provider returned an invalid response", null);
    }
}
