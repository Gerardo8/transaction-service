package com.spin.transactions.application.port.in;

import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;

/**
 * Inbound port for transaction queries.
 *
 * <p>Separated from {@link ExecuteTransactionUseCase} so command and query ports can evolve independently.
 */
public interface GetTransactionUseCase {

    /**
     * Searches transactions using optional filters and zero-based pagination.
     *
     * @param accountId optional exact account identifier
     * @param status optional transaction status
     * @param type optional transaction type
     * @param page zero-based page index
     * @param limit number of items per page
     * @return matching transactions and total count
     */
    TransactionPage search(
            String accountId, TransactionStatus status, TransactionType type, int page, int limit);
}
