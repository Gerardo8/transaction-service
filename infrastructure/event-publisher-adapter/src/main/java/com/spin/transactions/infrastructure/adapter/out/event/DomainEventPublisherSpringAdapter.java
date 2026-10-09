package com.spin.transactions.infrastructure.adapter.out.event;

import com.spin.transactions.domain.event.DomainEvent;
import com.spin.transactions.application.port.out.DomainEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Infrastructure adapter that publishes domain events through Spring's
 * {@link ApplicationEventPublisher}.
 *
 * <p>This keeps the domain free of messaging technology. A Kafka or SNS
 * implementation of {@link DomainEventPublisherPort} can replace this adapter
 * without changing application or domain code.
 */
@Component
public class DomainEventPublisherSpringAdapter implements DomainEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherSpringAdapter.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates the adapter using Spring's event publisher.
     *
     * @param applicationEventPublisher Spring event publisher
     */
    public DomainEventPublisherSpringAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publishes each event through Spring's application event mechanism.
     *
     * @param events domain events to publish
     */
    @Override
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (DomainEvent event : events) {
            log.info("Publishing domain event {} for transaction {}", event.getClass().getSimpleName(), event.transactionId());
            this.applicationEventPublisher.publishEvent(event);
        }
    }
}
