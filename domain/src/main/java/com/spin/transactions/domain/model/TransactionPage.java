package com.spin.transactions.domain.model;

import java.util.List;

/**
 * Immutable page of transactions returned from a search.
 *
 * @param items transaction items on the current page
 * @param page zero-based page number
 * @param limit maximum number of items per page
 * @param totalElements total number of matching transactions
 */
public record TransactionPage(List<Transaction> items, int page, int limit, long totalElements) {
    /** Copies the items so callers cannot mutate the page contents. */
    public TransactionPage {
        items = List.copyOf(items);
    }
}
