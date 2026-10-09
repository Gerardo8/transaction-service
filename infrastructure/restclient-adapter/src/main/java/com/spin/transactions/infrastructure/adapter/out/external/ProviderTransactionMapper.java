package com.spin.transactions.infrastructure.adapter.out.external;

import com.spin.transactions.domain.exception.ProviderUnavailableException;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.infrastructure.adapter.out.external.dto.ProviderErrorResponse;
import com.spin.transactions.infrastructure.adapter.out.external.dto.ProviderExecutionRequest;
import com.spin.transactions.infrastructure.adapter.out.external.dto.ProviderExecutionResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Locale;

/** Converts between the application's provider-neutral model and the HTTP provider contract. */
final class ProviderTransactionMapper {

    private ProviderTransactionMapper() {
    }

    /** Maps an internal transaction to the provider execution request. */
    static ProviderExecutionRequest toRequest(Transaction transaction) {
        return new ProviderExecutionRequest(
                transaction.getAccountId().value(),
                transaction.getType().name(),
                transaction.getMoney().amount(),
                transaction.getMoney().currency().getCurrencyCode());
    }

    /** Maps an HTTP provider response to a normalized domain result. */
    static ProviderTransactionResult toResult(ProviderExecutionResponse response) {
        if (response.status() == null) {
            throw invalidProviderResponse();
        }
        return switch (response.status().toUpperCase(Locale.ROOT)) {
            case "APPROVED" -> {
                if (response.transactionId() == null || response.balance() == null || response.executedAt() == null) {
                    throw invalidProviderResponse();
                }
                yield ProviderTransactionResult.approved(
                        response.transactionId(), response.balance(), response.executedAt());
            }
            case "REJECTED" -> ProviderTransactionResult.rejected(
                    response.transactionId(),
                    response.balance(),
                    response.executedAt(),
                    response.code() == null ? "PROVIDER_REJECTED" : response.code(),
                    response.message() == null ? "Provider rejected the transaction" : response.message());
            default -> throw invalidProviderResponse();
        };
    }

    /** Decodes a provider's structured client-error payload when available. */
    static ProviderErrorResponse toError(HttpClientErrorException error) {
        try {
            ProviderErrorResponse response = error.getResponseBodyAs(ProviderErrorResponse.class);
            return response == null ? new ProviderErrorResponse(null, null, null) : response;
        } catch (RestClientException ignored) {
            return new ProviderErrorResponse(null, null, null);
        }
    }

    private static ProviderUnavailableException invalidProviderResponse() {
        return new ProviderUnavailableException(
                "INVALID_PROVIDER_RESPONSE", "Provider returned an invalid response", null);
    }
}
