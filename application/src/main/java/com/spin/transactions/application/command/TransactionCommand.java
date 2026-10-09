package com.spin.transactions.application.command;

import java.math.BigDecimal;

/**
 * Application-layer command for executing a transaction.
 *
 * <p>Commands describe an intent from a driving adapter; they are not domain objects.
 *
 * @param accountId identifier of the account affected by the transaction
 * @param amount monetary amount to execute
 * @param currency ISO currency code
 * @param type transaction direction, CREDIT or DEBIT
 * @param description optional client-provided description
 * @param idempotencyKey key used to identify retries of this request
 */
public record TransactionCommand(
        String accountId,
        BigDecimal amount,
        String currency,
        String type,
        String description,
        String idempotencyKey
) {}
