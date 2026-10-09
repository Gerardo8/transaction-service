package com.spin.transactions.application.port.in;

import com.spin.transactions.application.command.TransactionCommand;
import com.spin.transactions.domain.model.Transaction;

/**
 * Inbound port for the execute-transaction command use case.
 *
 * <p>One interface, one use case (Interface Segregation).
 */
public interface ExecuteTransactionUseCase {

    /**
     * Executes a transaction and returns the persisted provider outcome.
     *
     * @param command validated transaction intent and idempotency key
     * @return the transaction in its current persisted state
     */
    Transaction execute(TransactionCommand command);
}
