package com.spin.transactions.domain.model;

/**
 * Direction of a transaction from the perspective of the account.
 */
public enum TransactionType {
    /** Adds funds to the account. */
    CREDIT,
    /** Removes funds from the account, subject to the per-transaction limit. */
    DEBIT
}
