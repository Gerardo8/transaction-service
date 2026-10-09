package com.spin.transactions.application.port.out;

import com.spin.transactions.domain.event.DomainEvent;

import java.util.List;

/**
 * Outbound port for publishing domain events.
 *
 * <p>Infrastructure adapters (Kafka, SNS, Spring events, etc.) implement this
 * without leaking messaging concerns into the application.
 */
public interface DomainEventPublisherPort {

    /**
     * Publishes events raised while processing a transaction.
     *
     * @param events domain events to publish; implementations may treat an empty list as a no-op
     */
    void publish(List<DomainEvent> events);
}
