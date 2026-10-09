package com.spin.transactions.infrastructure.adapter.out.persistence;

import com.spin.transactions.application.port.out.TransactionRepositoryPort;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements the application transaction repository port using Spring Data JDBC.
 */
@Component
public class TransactionRepositoryJdbcAdapter implements TransactionRepositoryPort {

    private final TransactionEntityRepository transactionEntityRepository;

    /**
     * Creates the repository adapter.
     *
     * @param transactionEntityRepository Spring Data transaction repository
     */
    public TransactionRepositoryJdbcAdapter(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    /** Persists the aggregate and its idempotency key. */
    @Override
    public Transaction save(Transaction transaction, String idempotencyKey) {
        final var version = this.transactionEntityRepository.findById(transaction.getId())
                .map(TransactionEntity::version)
                .orElse(null);
        this.transactionEntityRepository.save(
                TransactionEntityMapper.toEntity(transaction, idempotencyKey, version));
        return transaction;
    }

    /** Loads an aggregate by its service-generated identifier. */
    @Override
    public Optional<Transaction> findById(UUID id) {
        return this.transactionEntityRepository.findById(id).map(TransactionEntityMapper::toDomain);
    }

    /** Loads an existing aggregate by the original client request key. */
    @Override
    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {
        return this.transactionEntityRepository.findByIdempotencyKey(idempotencyKey)
                .map(TransactionEntityMapper::toDomain);
    }

    /** Delegates filtered paging to the custom JDBC search implementation. */
    @Override
    public TransactionPage search(
            String accountId, TransactionStatus status, TransactionType type, int page, int limit) {
        return this.transactionEntityRepository.searchTransactions(accountId, status, type, page, limit);
    }
}
