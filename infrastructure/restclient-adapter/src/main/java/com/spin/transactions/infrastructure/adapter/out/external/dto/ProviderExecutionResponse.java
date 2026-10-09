package com.spin.transactions.infrastructure.adapter.out.external.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response contract returned by the external provider for a transaction.
 *
 * @param transactionId provider transaction identifier
 * @param status approval or rejection status
 * @param balance account balance after execution
 * @param executedAt provider execution time
 * @param code provider rejection code
 * @param message provider rejection detail
 */
public record ProviderExecutionResponse(
        String transactionId,
        String status,
        BigDecimal balance,
        Instant executedAt,
        String code,
        String message) {
}
