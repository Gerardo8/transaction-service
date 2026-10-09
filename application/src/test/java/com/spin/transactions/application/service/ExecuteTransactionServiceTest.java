package com.spin.transactions.application.service;

import com.spin.transactions.application.command.TransactionCommand;
import com.spin.transactions.application.fixtures.TransactionCommandFixtures;
import com.spin.transactions.application.port.out.DomainEventPublisherPort;
import com.spin.transactions.application.port.out.ExternalProviderPort;
import com.spin.transactions.application.port.out.TransactionRepositoryPort;
import com.spin.transactions.application.port.out.UnitOfWorkPort;
import com.spin.transactions.domain.event.DomainEvent;
import com.spin.transactions.domain.event.TransactionExecutedEvent;
import com.spin.transactions.domain.event.TransactionRejectedEvent;
import com.spin.transactions.domain.exception.BusinessRuleViolationException;
import com.spin.transactions.domain.exception.ProviderUnavailableException;
import com.spin.transactions.domain.exception.TransactionLimitExceededException;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecuteTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;
    @Mock
    private ExternalProviderPort externalProviderPort;
    @Mock
    private DomainEventPublisherPort domainEventPublisher;
    @Mock
    private UnitOfWorkPort unitOfWork;

    private ExecuteTransactionService service;

    @BeforeEach
    void setUp() {
        service = new ExecuteTransactionService(
                transactionRepository, externalProviderPort, domainEventPublisher, unitOfWork);
        lenient().when(unitOfWork.execute(any())).thenAnswer(invocation ->
                invocation.<java.util.function.Supplier<Transaction>>getArgument(0).get());
    }

    @Test
    void runsUseCaseOnCallingThread() {
        Transaction existing = TransactionFixtures.anExecutedTransaction();
        TransactionCommand command = TransactionCommandFixtures.aCreditCommand();
        Thread callingThread = Thread.currentThread();
        AtomicReference<Thread> repositoryThread = new AtomicReference<>();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenAnswer(invocation -> {
            repositoryThread.set(Thread.currentThread());
            return Optional.of(existing);
        });

        assertEquals(existing, service.execute(command));
        assertEquals(callingThread, repositoryThread.get());
    }

    @Test
    void returnsExistingTransactionOnIdempotencyHit() {
        Transaction existing = TransactionFixtures.anExecutedTransaction();
        TransactionCommand command = TransactionCommandFixtures.aCreditCommand();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.of(existing));

        assertEquals(existing, service.execute(command));
        verify(externalProviderPort, never()).processTransaction(any(), anyString());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void marksExecutedAndPublishesEventWhenProviderSucceeds() {
        TransactionCommand command = TransactionCommandFixtures.aCreditCommand();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ProviderTransactionResult providerResult = ProviderTransactionResult.approved(
                "provider-001", new BigDecimal("1200.00"), Instant.parse("2025-03-15T10:30:00Z"));
        when(externalProviderPort.processTransaction(any(Transaction.class), anyString())).thenReturn(providerResult);

        Transaction result = service.execute(command);
        assertEquals(TransactionStatus.EXECUTED, result.getStatus());
        assertEquals("provider-001", result.getProviderTransactionId());
        assertEquals(new BigDecimal("1200.00"), result.getBalanceAfter());
        assertEquals(Instant.parse("2025-03-15T10:30:00Z"), result.getExecutedAt());
        assertEquals("Test credit", result.getDescription());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> events = ArgumentCaptor.forClass(List.class);
        verify(domainEventPublisher).publish(events.capture());
        assertEquals(1, events.getValue().size());
        assertInstanceOf(TransactionExecutedEvent.class, events.getValue().getFirst());
    }

    @Test
    void marksRejectedAndPublishesEventWhenProviderRejects() {
        TransactionCommand command = TransactionCommandFixtures.aDebitCommand();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalProviderPort.processTransaction(any(Transaction.class), anyString()))
                .thenReturn(ProviderTransactionResult.rejected(
                        null, null, null, "INSUFFICIENT_FUNDS", "Not enough balance"));

        Transaction result = service.execute(command);
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals("INSUFFICIENT_FUNDS", result.getErrorCode());
        assertEquals("Not enough balance", result.getErrorMessage());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> events = ArgumentCaptor.forClass(List.class);
        verify(domainEventPublisher).publish(events.capture());
        assertInstanceOf(TransactionRejectedEvent.class, events.getValue().getFirst());
    }

    @Test
    void marksFailedWhenProviderThrowsBeforeReturningAResult() {
        TransactionCommand command = TransactionCommandFixtures.aCreditCommand();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalProviderPort.processTransaction(any(Transaction.class), anyString()))
                .thenThrow(new ProviderUnavailableException("PROVIDER_TIMEOUT", "Timed out", null));

        Transaction result = service.execute(command);
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals("PROVIDER_TIMEOUT", result.getErrorCode());
        verify(domainEventPublisher).publish(List.of());
    }

    @Test
    void propagatesUnexpectedProviderFailures() {
        TransactionCommand command = TransactionCommandFixtures.aCreditCommand();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalProviderPort.processTransaction(any(Transaction.class), anyString()))
                .thenThrow(new IllegalStateException("unexpected"));

        assertInstanceOf(IllegalStateException.class,
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalStateException.class, () -> service.execute(command)));
    }

    @Test
    void oversizedDebitViolatesLimitBeforeCallingProvider() {
        TransactionCommand command = TransactionCommandFixtures.anOversizedDebitCommand();
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());

        assertServiceFailure(TransactionLimitExceededException.class, () -> service.execute(command));
        verify(externalProviderPort, never()).processTransaction(any(), anyString());
    }

    @Test
    void rejectsAmountsAtOrBelowOneBeforeCallingProvider() {
        TransactionCommand command = TransactionCommandFixtures.aCommand(
                "ACC-001", "1.00", "MXN", "CREDIT", "Test credit", "amount-too-small");
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());

        assertServiceFailure(BusinessRuleViolationException.class, () -> service.execute(command));
        verify(externalProviderPort, never()).processTransaction(any(), anyString());
    }

    @Test
    void rejectsUnsupportedCurrencyBeforeCallingProvider() {
        TransactionCommand command = TransactionCommandFixtures.aCommand(
                "ACC-001", "10.00", "USD", "CREDIT", "Test credit", "unsupported-currency");
        when(transactionRepository.findByIdempotencyKey(command.idempotencyKey())).thenReturn(Optional.empty());

        assertServiceFailure(BusinessRuleViolationException.class, () -> service.execute(command));
        verify(externalProviderPort, never()).processTransaction(any(), anyString());
    }

    private void assertServiceFailure(Class<? extends Throwable> expected, Runnable action) {
        org.junit.jupiter.api.Assertions.assertThrows(expected, action::run);
    }
}
