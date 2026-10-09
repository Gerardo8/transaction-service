package com.spin.transactions.infrastructure.adapter.out.persistence;

import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;

/**
 * Custom query surface for filtered and paginated transaction searches.
 */
public interface TransactionEntitySearchRepository {

    /**
     * Finds one page of transactions matching the supplied filters.
     *
     * @param accountId optional account filter
     * @param status optional status filter
     * @param type optional direction filter
     * @param page zero-based page number
     * @param limit page size
     * @return matching transactions and count
     */
    TransactionPage searchTransactions(
            String accountId, TransactionStatus status, TransactionType type, int page, int limit);
}
