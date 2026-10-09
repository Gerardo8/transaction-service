package com.spin.transactions.infrastructure.adapter.out.external.dto;

/**
 * Error payload returned by the external transaction provider.
 *
 * @param status provider outcome label
 * @param code provider-specific rejection code
 * @param message human-readable rejection reason
 */
public record ProviderErrorResponse(String status, String code, String message) {
}
