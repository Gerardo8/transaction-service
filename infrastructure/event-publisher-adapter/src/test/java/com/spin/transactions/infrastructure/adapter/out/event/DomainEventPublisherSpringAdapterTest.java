package com.spin.transactions.infrastructure.adapter.out.event;

import com.spin.transactions.domain.event.TransactionExecutedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherSpringAdapterTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DomainEventPublisherSpringAdapter publisher;

    @Test
    void publishesEachEvent() {
        TransactionExecutedEvent event = new TransactionExecutedEvent(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ACC-001",
                Instant.parse("2024-01-01T12:00:00Z")
        );

        publisher.publish(List.of(event));

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void ignoresEmptyList() {
        publisher.publish(List.of());
        verifyNoInteractions(applicationEventPublisher);
    }
}
