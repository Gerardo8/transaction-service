package com.spin.transactions.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event raised by a domain operation and published after its state is persisted.
 */
public interface DomainEvent {

    /**
     * Returns the identifier of the transaction that raised the event.
     *
     * @return transaction identifier
     */
    UUID transactionId();

    /**
     * Returns the time at which the domain event occurred.
     *
     * @return event occurrence time
     */
    Instant occurredAt();
}
