CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    account_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    provider_transaction_id VARCHAR(255),
    balance_after DECIMAL(19, 2),
    executed_at TIMESTAMP WITH TIME ZONE,
    error_code VARCHAR(100),
    error_message VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);
