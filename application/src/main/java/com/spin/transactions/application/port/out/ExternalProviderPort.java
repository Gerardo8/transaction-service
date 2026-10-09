package com.spin.transactions.application.port.out;

import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;

/**
 * Outbound port for delegating account balance and transaction execution to the provider.
 */
public interface ExternalProviderPort {

    /**
     * Submits a transaction to the provider using the same idempotency key as the client request.
     *
     * @param transaction validated transaction to execute
     * @param idempotencyKey stable key for provider-side duplicate protection
     * @return the normalized provider outcome
     */
    ProviderTransactionResult processTransaction(Transaction transaction, String idempotencyKey);
}
