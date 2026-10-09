package com.spin.transactions.domain.model;

/**
 * Lifecycle state of a transaction tracked by this service.
 */
public enum TransactionStatus {
    /** The request is recorded and awaiting a final provider outcome. */
    PENDING,
    /** The provider approved and executed the transaction. */
    EXECUTED,
    /** The provider declined the transaction. */
    REJECTED,
    /** The service could not confirm a provider outcome. */
    FAILED
}
