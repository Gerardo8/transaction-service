package com.spin.transactions.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Provider outcome normalized for use by the application and transaction aggregate.
 *
 * @param outcome normalized approval, rejection, or failure
 * @param providerTransactionId provider-assigned identifier, when supplied
 * @param balanceAfter provider-reported post-transaction balance, when supplied
 * @param executedAt provider-reported execution time, when supplied
 * @param errorCode provider or transport error code, when applicable
 * @param errorMessage explanatory error detail, when applicable
 */
public record ProviderTransactionResult(
        ProviderTransactionOutcome outcome,
        String providerTransactionId,
        BigDecimal balanceAfter,
        Instant executedAt,
        String errorCode,
        String errorMessage) {

    /**
     * Creates a successful provider outcome.
     *
     * @param transactionId provider-assigned transaction identifier
     * @param balance provider-reported resulting balance
     * @param executedAt provider-reported execution time
     * @return approved result
     */
    public static ProviderTransactionResult approved(String transactionId, BigDecimal balance, Instant executedAt) {
        return new ProviderTransactionResult(
                ProviderTransactionOutcome.APPROVED, transactionId, balance, executedAt, null, null);
    }

    /**
     * Creates a business rejection returned by the provider.
     *
     * @param transactionId provider identifier, if present
     * @param balance provider-reported balance, if present
     * @param executedAt provider-reported time, if present
     * @param code provider rejection code
     * @param message provider rejection message
     * @return rejected result
     */
    public static ProviderTransactionResult rejected(String transactionId, BigDecimal balance, Instant executedAt,
                                                     String code, String message) {
        return new ProviderTransactionResult(
                ProviderTransactionOutcome.REJECTED, transactionId, balance, executedAt, code, message);
    }

    /**
     * Creates a failure for calls whose outcome is unavailable or uncertain.
     *
     * @param code stable service error code
     * @param message diagnostic message safe to persist with the transaction
     * @return failed result
     */
    public static ProviderTransactionResult failed(String code, String message) {
        return new ProviderTransactionResult(ProviderTransactionOutcome.FAILED, null, null, null, code, message);
    }
}
