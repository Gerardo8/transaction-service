ALTER TABLE transactions
    ADD COLUMN description VARCHAR(1000),
    ADD COLUMN provider_transaction_id VARCHAR(255),
    ADD COLUMN balance_after DECIMAL(19, 2),
    ADD COLUMN executed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN error_code VARCHAR(100),
    ADD COLUMN error_message VARCHAR(1000);

-- Supports searches filtered by transaction direction.
CREATE INDEX idx_transactions_type ON transactions(type);
