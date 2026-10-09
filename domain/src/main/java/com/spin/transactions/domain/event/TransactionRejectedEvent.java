package com.spin.transactions.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event indicating that the provider rejected a transaction.
 *
 * @param transactionId service transaction identifier
 * @param accountId affected account identifier
 * @param occurredAt event occurrence time
 */
public record TransactionRejectedEvent(
        UUID transactionId,
        String accountId,
        Instant occurredAt
) implements DomainEvent {}
