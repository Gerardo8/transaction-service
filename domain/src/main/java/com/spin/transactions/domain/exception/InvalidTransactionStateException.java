package com.spin.transactions.domain.exception;

/**
 * Indicates that a transaction lifecycle operation is invalid for its current state.
 */
public class InvalidTransactionStateException extends BusinessRuleViolationException {

    /**
     * Creates an exception describing the invalid state transition.
     *
     * @param message explanation of the invalid transition
     */
    public InvalidTransactionStateException(String message) {
        super(message);
    }
}
