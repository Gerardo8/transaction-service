package com.spin.transactions.domain.exception;

/**
 * Base exception for violations of domain-level transaction behavior.
 */
public class DomainException extends RuntimeException {

    /**
     * Creates a domain exception with a client-readable explanation.
     *
     * @param message description of the violation
     */
    public DomainException(String message) {
        super(message);
    }
}
