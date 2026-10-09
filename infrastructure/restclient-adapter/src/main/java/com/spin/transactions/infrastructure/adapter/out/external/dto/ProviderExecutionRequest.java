package com.spin.transactions.infrastructure.adapter.out.external.dto;

import java.math.BigDecimal;

/**
 * Request contract for the provider's transaction execution endpoint.
 *
 * @param accountId account to debit or credit
 * @param type CREDIT or DEBIT
 * @param amount amount to execute
 * @param currency currency code
 */
public record ProviderExecutionRequest(String accountId, String type, BigDecimal amount, String currency) {
}
