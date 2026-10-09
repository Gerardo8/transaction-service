package com.spin.transactions.infrastructure.adapter.in.web.dto;

import com.spin.transactions.application.command.TransactionCommand;

/** Maps inbound HTTP request data to the application execute command. */
public final class TransactionRequestMapper {

    private TransactionRequestMapper() {
    }

    /**
     * Copies the validated request fields and request header into the application command.
     *
     * @param request validated HTTP request body
     * @param idempotencyKey request header used to deduplicate retries
     * @return application command
     */
    public static TransactionCommand toCommand(TransactionRequest request, String idempotencyKey) {
        return new TransactionCommand(
                request.accountId(),
                request.amount(),
                request.currency(),
                request.type(),
                request.description(),
                idempotencyKey);
    }
}
