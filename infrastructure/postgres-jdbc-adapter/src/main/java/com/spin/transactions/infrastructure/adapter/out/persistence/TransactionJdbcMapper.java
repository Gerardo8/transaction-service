package com.spin.transactions.infrastructure.adapter.out.persistence;

import com.spin.transactions.domain.model.AccountId;
import com.spin.transactions.domain.model.Money;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

/** Maps JDBC result rows to reconstructed domain transaction aggregates. */
final class TransactionJdbcMapper {

    static final RowMapper<Transaction> ROW_MAPPER = TransactionJdbcMapper::toDomain;

    private TransactionJdbcMapper() {
    }

    /** Maps one SQL result row, including nullable provider result fields. */
    static Transaction toDomain(ResultSet row, int rowNumber) throws SQLException {
        return Transaction.reconstruct(
                UUID.fromString(row.getString("id")),
                new AccountId(row.getString("account_id")),
                new Money(row.getBigDecimal("amount"), Currency.getInstance(row.getString("currency"))),
                TransactionType.valueOf(row.getString("type")),
                TransactionStatus.valueOf(row.getString("status")),
                instant(row.getTimestamp("created_at")),
                row.getString("description"),
                row.getString("provider_transaction_id"),
                row.getBigDecimal("balance_after"),
                instant(row.getTimestamp("executed_at")),
                row.getString("error_code"),
                row.getString("error_message"));
    }

    /** Converts an optional JDBC timestamp to the domain time representation. */
    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
