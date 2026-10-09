package com.spin.transactions.domain.model;

import com.spin.transactions.domain.exception.BusinessRuleViolationException;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void createsValidMxnAmountFromFixture() {
        Money money = TransactionFixtures.moneyMxn("500.00");

        assertEquals(new BigDecimal("500.00"), money.amount());
        assertEquals("MXN", money.currency().getCurrencyCode());
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(BusinessRuleViolationException.class,
                () -> new Money(null, Currency.getInstance("MXN")));
    }

    @Test
    void rejectsNullCurrency() {
        assertThrows(BusinessRuleViolationException.class,
                () -> new Money(new BigDecimal("10.00"), null));
    }

    @Test
    void rejectsNonMxnCurrency() {
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> new Money(new BigDecimal("10.00"), Currency.getInstance("USD")));
        assertEquals("Only MXN currency is supported", ex.getMessage());
    }

    @Test
    void rejectsAmountOfOneOrLess() {
        assertThrows(BusinessRuleViolationException.class,
                () -> TransactionFixtures.moneyMxn("1.00"));
        assertThrows(BusinessRuleViolationException.class,
                () -> TransactionFixtures.moneyMxn("0.50"));
    }

    @Test
    void equalityIgnoresScale() {
        Money left = TransactionFixtures.moneyMxn("10.00");
        Money right = new Money(new BigDecimal("10.0"), Currency.getInstance("MXN"));

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, TransactionFixtures.moneyMxn("11.00"));
    }
}
