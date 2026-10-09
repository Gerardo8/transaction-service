package com.spin.transactions.infrastructure.adapter.out.persistence;

import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implements parameterized JDBC searches so filters and pagination stay in the database.
 */
class TransactionEntitySearchRepositoryImpl implements TransactionEntitySearchRepository {

    private static final String SELECT_COLUMNS = """
            id, account_id, amount, currency, type, status, created_at, description,
            provider_transaction_id, balance_after, executed_at, error_code, error_message
            """;

    private final JdbcTemplate jdbcTemplate;

    TransactionEntitySearchRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Builds a filtered query with bound values, a deterministic sort, and a count query.
     */
    @Override
    public TransactionPage searchTransactions(
            String accountId, TransactionStatus status, TransactionType type, int page, int limit) {
        var filters = new ArrayList<String>();
        var parameters = new ArrayList<>();
        if (accountId != null && !accountId.isBlank()) {
            filters.add("account_id = ?");
            parameters.add(accountId);
        }
        if (status != null) {
            filters.add("status = ?");
            parameters.add(status.name());
        }
        if (type != null) {
            filters.add("type = ?");
            parameters.add(type.name());
        }

        final var whereClause = filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters);
        final var totalElements = this.jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions" + whereClause, Long.class, parameters.toArray());
        final var pageParameters = new ArrayList<>(parameters);
        pageParameters.add(limit);
        pageParameters.add((long) page * limit);
        final List<Transaction> items = this.jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM transactions" + whereClause
                        + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                TransactionJdbcMapper.ROW_MAPPER,
                pageParameters.toArray());
        return new TransactionPage(items, page, limit, Objects.requireNonNull(totalElements));
    }
}
