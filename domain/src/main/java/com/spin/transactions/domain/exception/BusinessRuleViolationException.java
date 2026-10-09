package com.spin.transactions.domain.exception;

/**
 * Indicates that a requested transaction violates a domain business rule.
 */
public class BusinessRuleViolationException extends DomainException {

    /**
     * Creates an exception describing the rejected business rule.
     *
     * @param message explanation of the rule violation
     */
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
