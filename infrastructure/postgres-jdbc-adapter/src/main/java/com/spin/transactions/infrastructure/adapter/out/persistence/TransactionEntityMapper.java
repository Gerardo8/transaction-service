package com.spin.transactions.infrastructure.adapter.out.persistence;

import com.spin.transactions.domain.model.AccountId;
import com.spin.transactions.domain.model.Money;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;

import java.util.Currency;

/** Converts transaction aggregates to and from their Spring Data JDBC row representation. */
final class TransactionEntityMapper {

    private TransactionEntityMapper() {
    }

    /** Maps a domain aggregate to a persistence entity. */
    static TransactionEntity toEntity(Transaction transaction, String idempotencyKey, Long version) {
        return new TransactionEntity(
                transaction.getId(),
                transaction.getAccountId().value(),
                transaction.getMoney().amount(),
                transaction.getMoney().currency().getCurrencyCode(),
                transaction.getType().name(),
                transaction.getStatus().name(),
                transaction.getCreatedAt(),
                idempotencyKey,
                transaction.getDescription(),
                transaction.getProviderTransactionId(),
                transaction.getBalanceAfter(),
                transaction.getExecutedAt(),
                transaction.getErrorCode(),
                transaction.getErrorMessage(),
                version);
    }

    /** Reconstructs a domain aggregate from a persistence entity. */
    static Transaction toDomain(TransactionEntity entity) {
        return Transaction.reconstruct(
                entity.id(),
                new AccountId(entity.accountId()),
                new Money(entity.amount(), Currency.getInstance(entity.currency())),
                TransactionType.valueOf(entity.type()),
                TransactionStatus.valueOf(entity.status()),
                entity.createdAt(),
                entity.description(),
                entity.providerTransactionId(),
                entity.balanceAfter(),
                entity.executedAt(),
                entity.errorCode(),
                entity.errorMessage());
    }
}
