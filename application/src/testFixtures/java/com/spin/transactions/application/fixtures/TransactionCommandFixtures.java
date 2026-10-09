package com.spin.transactions.application.fixtures;

import com.spin.transactions.application.command.TransactionCommand;

import java.math.BigDecimal;

/**
 * Test Object Mother for {@link TransactionCommand}.
 *
 * <p>Shared across Gradle submodules via the {@code java-test-fixtures} plugin.
 *
 * <p>Usage in any submodule:
 * <pre>{@code
 * testImplementation(testFixtures(project(":application")))
 * }</pre>
 */
public final class TransactionCommandFixtures {

    private TransactionCommandFixtures() {}

    /** A valid CREDIT command in MXN. */
    public static TransactionCommand aCreditCommand() {
        return new TransactionCommand(
                "ACC-001",
                new BigDecimal("500.00"),
                "MXN",
                "CREDIT",
                "Test credit",
                "idempotency-key-001"
        );
    }

    /** A valid DEBIT command in MXN. */
    public static TransactionCommand aDebitCommand() {
        return new TransactionCommand(
                "ACC-001",
                new BigDecimal("200.00"),
                "MXN",
                "DEBIT",
                "Test debit",
                "idempotency-key-002"
        );
    }

    /** A command that violates the max DEBIT limit (>$10,000 MXN). */
    public static TransactionCommand anOversizedDebitCommand() {
        return new TransactionCommand(
                "ACC-001",
                new BigDecimal("15000.00"),
                "MXN",
                "DEBIT",
                "Oversized debit",
                "idempotency-key-003"
        );
    }

    /** A command builder for custom scenarios. */
    public static TransactionCommand aCommand(
            String accountId,
            String amount,
            String currency,
            String type,
            String description,
            String idempotencyKey) {
        return new TransactionCommand(
                accountId,
                new BigDecimal(amount),
                currency,
                type,
                description,
                idempotencyKey
        );
    }
}
