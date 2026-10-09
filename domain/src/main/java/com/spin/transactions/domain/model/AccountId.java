package com.spin.transactions.domain.model;

import com.spin.transactions.domain.exception.BusinessRuleViolationException;

/**
 * Non-empty identifier for an account owned and managed by the external provider.
 *
 * @param value provider-assigned account identifier
 */
public record AccountId(String value) {
    /** Validates the account identifier before it enters the domain model. */
    public AccountId {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessRuleViolationException("AccountId cannot be null or empty");
        }
    }
}
