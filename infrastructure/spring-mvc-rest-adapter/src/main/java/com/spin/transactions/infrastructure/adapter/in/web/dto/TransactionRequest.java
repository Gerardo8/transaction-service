package com.spin.transactions.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * JSON request body for executing a credit or debit transaction.
 *
 * @param accountId account whose balance is managed by the external provider
 * @param amount transaction amount in the selected currency; must be greater than 1.00
 * @param currency supported transaction currency, currently MXN only
 * @param type transaction direction, CREDIT or DEBIT
 * @param description optional human-readable transaction description
 */
@Schema(description = "Transaction details submitted for provider execution")
public record TransactionRequest(
        @Schema(description = "Account identifier", example = "acc-123456")
        @NotBlank(message = "accountId is required")
        String accountId,

        @Schema(description = "Positive amount greater than 1.00", example = "1500.00", minimum = "1.01")
        @NotNull(message = "amount is required")
        @DecimalMin(value = "1.00", inclusive = false, message = "amount must be greater than 1.00")
        BigDecimal amount,

        @Schema(description = "Supported currency code", example = "MXN", allowableValues = {"MXN"})
        @NotBlank(message = "currency is required")
        String currency,

        @Schema(description = "Transaction direction", example = "CREDIT", allowableValues = {"CREDIT", "DEBIT"})
        @NotBlank(message = "type is required")
        String type,

        @Schema(description = "Optional description", example = "Transfer received", maxLength = 1000)
        @Size(max = 1000, message = "description cannot exceed 1000 characters")
        String description
) {}
