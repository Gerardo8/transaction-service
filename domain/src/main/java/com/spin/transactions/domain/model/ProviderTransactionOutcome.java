package com.spin.transactions.domain.model;

/**
 * Normalized outcome returned while processing an external provider response.
 */
public enum ProviderTransactionOutcome {
    /** The provider accepted and executed the transaction. */
    APPROVED,
    /** The provider declined the transaction according to a business rule. */
    REJECTED,
    /** The service could not confirm successful execution. */
    FAILED
}
