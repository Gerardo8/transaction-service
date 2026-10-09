package com.spin.transactions.infrastructure.adapter.out.persistence;

import com.spin.transactions.domain.fixtures.TransactionFixtures;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;
import com.spin.transactions.application.port.out.UnitOfWorkPort;
import com.spin.transactions.infrastructure.adapter.out.transaction.UnitOfWorkSpringAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = PersistenceAdapterTestApplication.class)
@Import({TransactionRepositoryJdbcAdapter.class, UnitOfWorkSpringAdapter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.flyway.enabled=false"
})
class TransactionRepositoryJdbcAdapterTest {

    @Autowired
    private TransactionRepositoryJdbcAdapter adapter;
    @Autowired
    private UnitOfWorkPort unitOfWork;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesAndFindsById() {
        Transaction pending = TransactionFixtures.aPendingCreditTransaction();

        adapter.save(pending, "slice-key-001");
        pending.markAsExecuted(ProviderTransactionResult.approved(
                "provider-001", new BigDecimal("800.00"), Instant.parse("2025-03-15T10:30:00Z")));
        adapter.save(pending, "slice-key-001");
        Transaction loaded = adapter.findById(pending.getId()).orElseThrow();

        assertEquals(pending.getId(), loaded.getId());
        assertEquals(pending.getAccountId(), loaded.getAccountId());
        assertEquals(TransactionStatus.EXECUTED, loaded.getStatus());
        assertEquals("provider-001", loaded.getProviderTransactionId());
        assertEquals(new BigDecimal("800.00"), loaded.getBalanceAfter());
        assertEquals(Instant.parse("2025-03-15T10:30:00Z"), loaded.getExecutedAt());
        assertEquals("Test credit", loaded.getDescription());
    }

    @Test
    void findsByIdempotencyKey() {
        Transaction pending = TransactionFixtures.aPendingDebitTransaction();

        adapter.save(pending, "slice-key-002");
        Transaction loaded = adapter.findByIdempotencyKey("slice-key-002").orElseThrow();

        assertEquals(pending.getId(), loaded.getId());
        assertEquals(TransactionStatus.PENDING, loaded.getStatus());
    }

    @Test
    void returnsNullForMissingRecords() {
        assertEquals(java.util.Optional.empty(), adapter.findById(UUID.randomUUID()));
        assertEquals(java.util.Optional.empty(), adapter.findByIdempotencyKey("missing-key"));
    }

    @Test
    void unitOfWorkRollsBackItsDatabaseWorkWhenItFails() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> unitOfWork.execute(() -> {
            jdbcTemplate.update("""
                    INSERT INTO transactions (
                        id, account_id, amount, currency, type, status, created_at, idempotency_key
                    ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                    """, id, "ACC-ROLLBACK", new BigDecimal("25.00"), "MXN", "CREDIT", "PENDING", "rollback-key");
            throw new IllegalStateException("rollback");
        }));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE id = ?", Integer.class, id);
        assertEquals(0, count);
    }

    @Test
    void searchesWithFiltersAndPagination() {
        Transaction firstCredit = TransactionFixtures.aPendingCreditTransaction();
        Transaction secondCredit = TransactionFixtures.aPendingCreditTransaction();
        Transaction debit = TransactionFixtures.aPendingDebitTransaction();
        adapter.save(firstCredit, "search-credit-001");
        adapter.save(secondCredit, "search-credit-002");
        adapter.save(debit, "search-debit-001");

        TransactionPage result = adapter.search(
                "ACC-001", TransactionStatus.PENDING, TransactionType.CREDIT, 1, 1);

        assertEquals(2, result.totalElements());
        assertEquals(1, result.items().size());
        assertEquals(1, result.page());
        assertEquals(1, result.limit());
        assertEquals(TransactionType.CREDIT, result.items().getFirst().getType());
    }
}
