package com.spin.transactions.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Page envelope returned by the transaction search endpoint.
 *
 * @param items transactions on this page
 * @param page zero-based page index
 * @param limit requested page size
 * @param totalElements total number of matches across all pages
 * @param totalPages total number of pages for the requested limit
 */
@Schema(description = "A page of matching transactions with result counts")
public record TransactionSearchResponse(
        @Schema(description = "Transactions in this page")
        List<TransactionResponse> items,
        int page,
        int limit,
        long totalElements,
        long totalPages) {
}
