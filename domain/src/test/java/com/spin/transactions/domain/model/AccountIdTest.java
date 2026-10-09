package com.spin.transactions.domain.model;

import com.spin.transactions.domain.exception.BusinessRuleViolationException;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountIdTest {

    @Test
    void createsAccountIdFromFixture() {
        AccountId accountId = TransactionFixtures.anAccountId("ACC-001");

        assertEquals("ACC-001", accountId.value());
        assertEquals(TransactionFixtures.anAccountId("ACC-001"), accountId);
        assertEquals(accountId.hashCode(), TransactionFixtures.anAccountId("ACC-001").hashCode());
        assertNotEquals(accountId, TransactionFixtures.anAccountId("ACC-002"));
    }

    @Test
    void rejectsNull() {
        assertThrows(BusinessRuleViolationException.class, () -> new AccountId(null));
    }

    @Test
    void rejectsBlank() {
        assertThrows(BusinessRuleViolationException.class, () -> TransactionFixtures.anAccountId("  "));
        assertThrows(BusinessRuleViolationException.class, () -> new AccountId(""));
    }
}
