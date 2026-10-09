package com.spin.transactions.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Spring Data JDBC representation of a row in the {@code transactions} table.
 *
 * @param id service transaction identifier
 * @param accountId associated account identifier
 * @param amount transaction amount
 * @param currency currency code
 * @param type transaction direction
 * @param status lifecycle state
 * @param createdAt service creation timestamp
 * @param idempotencyKey unique client request key
 * @param description optional client description
 * @param providerTransactionId provider transaction identifier
 * @param balanceAfter provider-reported account balance
 * @param executedAt provider-reported execution time
 * @param errorCode provider or transport error code
 * @param errorMessage provider or transport error detail
 * @param version optimistic locking version
 */
@Table("transactions")
public record TransactionEntity(
        @Id @Column("id") UUID id,
        @Column("account_id") String accountId,
        @Column("amount") BigDecimal amount,
        @Column("currency") String currency,
        @Column("type") String type,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("idempotency_key") String idempotencyKey,
        @Column("description") String description,
        @Column("provider_transaction_id") String providerTransactionId,
        @Column("balance_after") BigDecimal balanceAfter,
        @Column("executed_at") Instant executedAt,
        @Column("error_code") String errorCode,
        @Column("error_message") String errorMessage,
        @Version @Column("version") Long version) {
}
