package com.spin.transactions.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event indicating that the provider approved a transaction.
 *
 * @param transactionId service transaction identifier
 * @param accountId affected account identifier
 * @param occurredAt event occurrence time
 */
public record TransactionExecutedEvent(
        UUID transactionId,
        String accountId,
        Instant occurredAt
) implements DomainEvent {}
