package com.spin.transactions.domain.model;

import com.spin.transactions.domain.exception.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Immutable MXN amount used by the transaction domain.
 *
 * @param amount monetary value, strictly greater than 1.00
 * @param currency supported currency; currently MXN only
 */
public record Money(BigDecimal amount, Currency currency) {
    /** Enforces the supported currency and minimum transaction amount. */
    public Money {
        if (amount == null) {
            throw new BusinessRuleViolationException("Amount cannot be null");
        }
        if (currency == null) {
            throw new BusinessRuleViolationException("Currency cannot be null");
        }
        if (!currency.getCurrencyCode().equals("MXN")) {
            throw new BusinessRuleViolationException("Only MXN currency is supported");
        }
        if (amount.compareTo(BigDecimal.ONE) <= 0) {
            throw new BusinessRuleViolationException("Transaction amount must be greater than $1.00 MXN");
        }
    }

    /**
     * Compares monetary values numerically so scale differences do not affect equality.
     *
     * @param o value to compare
     * @return whether both amount and currency are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money(BigDecimal amount1, Currency currency1))) return false;
        return amount.compareTo(amount1) == 0 && currency.equals(currency1);
    }

    /**
     * Computes a hash consistent with scale-insensitive monetary equality.
     *
     * @return hash code for this amount and currency
     */
    @Override
    public int hashCode() {
        return 31 * amount.stripTrailingZeros().hashCode() + currency.hashCode();
    }
}
