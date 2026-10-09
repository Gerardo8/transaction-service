package com.spin.transactions.infrastructure.adapter.in.web.dto;

import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionPage;

/** Maps domain transaction models to their public HTTP response representation. */
public final class TransactionResponseMapper {

    private TransactionResponseMapper() {
    }

    /**
     * Maps all public transaction fields without exposing persistence entities.
     *
     * @param transaction transaction aggregate
     * @return API response model
     */
    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId().value(),
                transaction.getMoney().amount(),
                transaction.getMoney().currency().getCurrencyCode(),
                transaction.getType().name(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getProviderTransactionId(),
                transaction.getBalanceAfter(),
                transaction.getExecutedAt(),
                transaction.getErrorCode(),
                transaction.getErrorMessage(),
                transaction.getCreatedAt());
    }

    /**
     * Maps a domain page and calculates the number of pages for its page size.
     *
     * @param page domain transaction page
     * @return API page response
     */
    public static TransactionSearchResponse toSearchResponse(TransactionPage page) {
        long totalPages = (page.totalElements() + page.limit() - 1) / page.limit();
        return new TransactionSearchResponse(
                page.items().stream().map(TransactionResponseMapper::toResponse).toList(),
                page.page(),
                page.limit(),
                page.totalElements(),
                totalPages);
    }
}
