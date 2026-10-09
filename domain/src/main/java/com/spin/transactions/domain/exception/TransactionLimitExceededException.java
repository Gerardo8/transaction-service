package com.spin.transactions.domain.exception;

/**
 * Indicates that a transaction exceeds a configured per-operation limit.
 */
public class TransactionLimitExceededException extends BusinessRuleViolationException {

    /**
     * Creates an exception describing the exceeded transaction limit.
     *
     * @param message explanation of the limit
     */
    public TransactionLimitExceededException(String message) {
        super(message);
    }
}
