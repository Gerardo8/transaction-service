package com.spin.transactions.application.port.out;

import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for storing and querying transactions.
 */
public interface TransactionRepositoryPort {

    /**
     * Inserts or updates a transaction, associating it with the request's idempotency key.
     *
     * @param transaction transaction state to persist
     * @param idempotencyKey unique key used to find a previous request
     * @return the persisted transaction
     */
    Transaction save(Transaction transaction, String idempotencyKey);

    /**
     * Finds a transaction by its service-generated identifier.
     *
     * @param id transaction identifier
     * @return the transaction when present
     */
    Optional<Transaction> findById(UUID id);

    /**
     * Finds a prior result so a repeated request does not execute again.
     *
     * @param idempotencyKey request idempotency key
     * @return the previously persisted transaction when present
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Returns a page of transactions matching the optional filters.
     *
     * @param accountId optional exact account identifier
     * @param status optional status filter
     * @param type optional type filter
     * @param page zero-based page index
     * @param limit maximum number of results
     * @return matching page and total match count
     */
    TransactionPage search(
            String accountId, TransactionStatus status, TransactionType type, int page, int limit);
}
