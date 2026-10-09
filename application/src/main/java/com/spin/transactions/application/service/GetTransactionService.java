package com.spin.transactions.application.service;

import com.spin.transactions.application.port.in.GetTransactionUseCase;
import com.spin.transactions.application.port.out.TransactionRepositoryPort;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;

/** Orchestrates validation and execution of transaction search queries. */
public class GetTransactionService implements GetTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;

    /**
     * Creates the query use case with its persistence port.
     *
     * @param transactionRepository transaction search port
     */
    public GetTransactionService(TransactionRepositoryPort transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Validates pagination bounds and delegates the filtered search to persistence.
     *
     * @param accountId optional account filter
     * @param status optional status filter
     * @param type optional transaction type filter
     * @param page zero-based page index
     * @param limit number of results per page, from 1 through 100
     * @return requested transaction page
     * @throws IllegalArgumentException when page or limit is outside its supported range
     */
    @Override
    public TransactionPage search(
            String accountId, TransactionStatus status, TransactionType type, int page, int limit) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
        return this.transactionRepository.search(accountId, status, type, page, limit);
    }
}
