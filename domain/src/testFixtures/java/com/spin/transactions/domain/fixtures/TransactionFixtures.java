package com.spin.transactions.domain.fixtures;

import com.spin.transactions.domain.model.AccountId;
import com.spin.transactions.domain.model.Money;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

/**
 * Test Object Mother for {@link Transaction} domain objects.
 *
 * <p>Shared across all Gradle submodules via the {@code java-test-fixtures} plugin.
 * Use this in tests to avoid duplicating domain object construction logic.
 *
 * <p>Usage in any submodule:
 * <pre>{@code
 * testImplementation(testFixtures(project(":domain")))
 * }</pre>
 *
 * <p>Command fixtures live in {@code application} testFixtures because
 * {@code TransactionCommand} is an application-layer concept.
 */
public final class TransactionFixtures {

    private static final Currency MXN = Currency.getInstance("MXN");

    private TransactionFixtures() {}

    /** A standard PENDING CREDIT transaction ready for processing. */
    public static Transaction aPendingCreditTransaction() {
        return Transaction.create(
                new AccountId("ACC-001"),
                new Money(new BigDecimal("500.00"), MXN),
                TransactionType.CREDIT,
                "Test credit"
        );
    }

    /** A standard PENDING DEBIT transaction within the allowed limit. */
    public static Transaction aPendingDebitTransaction() {
        return Transaction.create(
                new AccountId("ACC-001"),
                new Money(new BigDecimal("200.00"), MXN),
                TransactionType.DEBIT,
                "Test debit"
        );
    }

    /** A reconstructed EXECUTED transaction (simulates a DB-loaded entity). */
    public static Transaction anExecutedTransaction() {
        return Transaction.reconstruct(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new AccountId("ACC-002"),
                new Money(new BigDecimal("1000.00"), MXN),
                TransactionType.CREDIT,
                TransactionStatus.EXECUTED,
                Instant.parse("2024-01-01T12:00:00Z"),
                "Test credit",
                "provider-001",
                new BigDecimal("2500.00"),
                Instant.parse("2024-01-01T12:00:01Z"),
                null,
                null
        );
    }

    /** A reconstructed REJECTED transaction. */
    public static Transaction aRejectedTransaction() {
        return Transaction.reconstruct(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                new AccountId("ACC-003"),
                new Money(new BigDecimal("150.00"), MXN),
                TransactionType.DEBIT,
                TransactionStatus.REJECTED,
                Instant.parse("2024-01-02T08:30:00Z"),
                "Test debit",
                null,
                null,
                null,
                "INSUFFICIENT_FUNDS",
                "Insufficient funds"
        );
    }

    /** A reconstructed FAILED transaction. */
    public static Transaction aFailedTransaction() {
        return Transaction.reconstruct(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                new AccountId("ACC-004"),
                new Money(new BigDecimal("300.00"), MXN),
                TransactionType.CREDIT,
                TransactionStatus.FAILED,
                Instant.parse("2024-01-03T09:00:00Z"),
                "Test credit",
                null,
                null,
                null,
                "PROVIDER_TIMEOUT",
                "Provider execution outcome is unknown"
        );
    }

    /** Helper: create an AccountId value object. */
    public static AccountId anAccountId(String value) {
        return new AccountId(value);
    }

    /** Helper: create a Money value object in MXN. */
    public static Money moneyMxn(String amount) {
        return new Money(new BigDecimal(amount), MXN);
    }
}
