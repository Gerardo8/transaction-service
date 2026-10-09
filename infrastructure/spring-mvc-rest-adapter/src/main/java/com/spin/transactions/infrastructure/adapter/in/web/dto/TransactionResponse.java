package com.spin.transactions.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Transaction representation returned by execution and search endpoints.
 *
 * @param id service-generated transaction identifier
 * @param accountId account identifier
 * @param amount submitted amount
 * @param currency currency code
 * @param type CREDIT or DEBIT
 * @param status current processing outcome
 * @param description optional client description
 * @param providerTransactionId identifier returned by the external provider, if available
 * @param balanceAfter provider-reported balance after execution, if available
 * @param executedAt provider-reported execution time, if available
 * @param errorCode provider or infrastructure failure code, if applicable
 * @param errorMessage descriptive failure detail, if applicable
 * @param createdAt time the service created the transaction
 */
@Schema(description = "Persisted transaction and its provider outcome")
public record TransactionResponse(
        @Schema(description = "Service-generated transaction identifier")
        UUID id,
        @Schema(example = "acc-123456")
        String accountId,
        @Schema(example = "1500.00")
        BigDecimal amount,
        @Schema(example = "MXN")
        String currency,
        @Schema(allowableValues = {"CREDIT", "DEBIT"})
        String type,
        @Schema(allowableValues = {"PENDING", "EXECUTED", "REJECTED", "FAILED"})
        String status,
        String description,
        String providerTransactionId,
        @Schema(description = "Balance reported by the provider after execution")
        BigDecimal balanceAfter,
        Instant executedAt,
        String errorCode,
        String errorMessage,
        @Schema(description = "Timestamp when the service accepted the transaction")
        Instant createdAt
) {
}
