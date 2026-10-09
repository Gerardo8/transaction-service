package com.spin.transactions.domain.model;

import com.spin.transactions.domain.event.TransactionExecutedEvent;
import com.spin.transactions.domain.event.TransactionRejectedEvent;
import com.spin.transactions.domain.exception.InvalidTransactionStateException;
import com.spin.transactions.domain.exception.TransactionLimitExceededException;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void createStartsAsPending() {
        Transaction transaction = TransactionFixtures.aPendingCreditTransaction();

        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals(TransactionType.CREDIT, transaction.getType());
        assertTrue(transaction.getDomainEvents().isEmpty());
    }

    @Test
    void debitWithinLimitIsAllowed() {
        Transaction transaction = TransactionFixtures.aPendingDebitTransaction();

        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals(TransactionType.DEBIT, transaction.getType());
    }

    @Test
    void debitAboveLimitIsRejected() {
        assertThrows(TransactionLimitExceededException.class, () ->
                Transaction.create(
                        TransactionFixtures.anAccountId("ACC-001"),
                        TransactionFixtures.moneyMxn("10000.01"),
                        TransactionType.DEBIT,
                        "Test debit"
                ));
    }

    @Test
    void markAsExecutedRecordsEvent() {
        Transaction transaction = TransactionFixtures.aPendingCreditTransaction();

        transaction.markAsExecuted(ProviderTransactionResult.approved(
                "provider-123", new BigDecimal("1500.00"), Instant.parse("2025-03-15T10:30:00Z")));

        assertEquals(TransactionStatus.EXECUTED, transaction.getStatus());
        assertEquals(1, transaction.getDomainEvents().size());
        assertInstanceOf(TransactionExecutedEvent.class, transaction.getDomainEvents().getFirst());
        assertEquals(transaction.getId(), transaction.pullDomainEvents().getFirst().transactionId());
        assertTrue(transaction.getDomainEvents().isEmpty());
    }

    @Test
    void markAsRejectedRecordsEvent() {
        Transaction transaction = TransactionFixtures.aPendingDebitTransaction();

        transaction.markAsRejected(ProviderTransactionResult.rejected(
                null, null, null, "INSUFFICIENT_FUNDS", "Insufficient funds"));

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertInstanceOf(TransactionRejectedEvent.class, transaction.pullDomainEvents().getFirst());
    }

    @Test
    void markAsFailedDoesNotRecordEvent() {
        Transaction transaction = TransactionFixtures.aPendingCreditTransaction();

        transaction.markAsFailed(ProviderTransactionResult.failed("PROVIDER_TIMEOUT", "Timed out"));

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        assertTrue(transaction.getDomainEvents().isEmpty());
    }

    @Test
    void cannotTransitionFromExecuted() {
        Transaction executed = TransactionFixtures.anExecutedTransaction();

        assertThrows(InvalidTransactionStateException.class, () -> executed.markAsExecuted(
                ProviderTransactionResult.approved("provider-123", new BigDecimal("1500"), Instant.now())));
        assertThrows(InvalidTransactionStateException.class, () -> executed.markAsRejected(
                ProviderTransactionResult.rejected(null, null, null, "REJECTED", "Rejected")));
        assertThrows(InvalidTransactionStateException.class, () -> executed.markAsFailed(
                ProviderTransactionResult.failed("PROVIDER_TIMEOUT", "Timed out")));
    }

    @Test
    void reconstructedRejectedTransactionHasNoPendingEvents() {
        Transaction rejected = TransactionFixtures.aRejectedTransaction();

        assertEquals(TransactionStatus.REJECTED, rejected.getStatus());
        assertTrue(rejected.getDomainEvents().isEmpty());
    }
}
