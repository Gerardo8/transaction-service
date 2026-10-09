package com.spin.transactions.domain.model;

import com.spin.transactions.domain.event.DomainEvent;
import com.spin.transactions.domain.event.TransactionExecutedEvent;
import com.spin.transactions.domain.event.TransactionRejectedEvent;
import com.spin.transactions.domain.exception.InvalidTransactionStateException;
import com.spin.transactions.domain.exception.TransactionLimitExceededException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate representing a requested transaction and the outcome returned by the provider.
 *
 * <p>The aggregate protects transaction limits and state transitions, and records domain events
 * that are published after the resulting state has been persisted.
 */
public class Transaction {
    private final UUID id;
    private final AccountId accountId;
    private final Money money;
    private final TransactionType type;
    private TransactionStatus status;
    private final Instant createdAt;
    private final String description;
    private String providerTransactionId;
    private BigDecimal balanceAfter;
    private Instant executedAt;
    private String errorCode;
    private String errorMessage;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /** Maximum permitted amount for a single debit transaction. */
    private static final BigDecimal MAX_DEBIT_AMOUNT = new BigDecimal("10000.00");

    private Transaction(UUID id, AccountId accountId, Money money, TransactionType type, TransactionStatus status,
                        Instant createdAt, String description, String providerTransactionId,
                        BigDecimal balanceAfter, Instant executedAt, String errorCode, String errorMessage) {
        this.id = id;
        this.accountId = accountId;
        this.money = money;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.description = description;
        this.providerTransactionId = providerTransactionId;
        this.balanceAfter = balanceAfter;
        this.executedAt = executedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a pending transaction after enforcing transaction-specific business rules.
     *
     * @param accountId account associated with the transaction
     * @param money supported amount and currency
     * @param type transaction direction
     * @param description optional client description
     * @return a new pending transaction
     * @throws TransactionLimitExceededException when a debit exceeds the maximum amount
     */
    public static Transaction create(AccountId accountId, Money money, TransactionType type, String description) {
        validateBusinessRules(money, type);
        return new Transaction(UUID.randomUUID(), accountId, money, type, TransactionStatus.PENDING, Instant.now(),
                description, null, null, null, null, null);
    }

    /**
     * Reconstructs a stored transaction including all provider and error details.
     *
     * @param id transaction identifier
     * @param accountId associated account
     * @param money transaction amount
     * @param type transaction direction
     * @param status persisted lifecycle state
     * @param createdAt creation time
     * @param description client description
     * @param providerTransactionId provider identifier, if available
     * @param balanceAfter provider-reported resulting balance, if available
     * @param executedAt provider-reported execution time, if available
     * @param errorCode failure code, if applicable
     * @param errorMessage failure detail, if applicable
     * @return reconstructed transaction
     */
    public static Transaction reconstruct(UUID id, AccountId accountId, Money money, TransactionType type,
                                          TransactionStatus status, Instant createdAt, String description,
                                          String providerTransactionId, BigDecimal balanceAfter, Instant executedAt,
                                          String errorCode, String errorMessage) {
        return new Transaction(id, accountId, money, type, status, createdAt, description, providerTransactionId,
                balanceAfter, executedAt, errorCode, errorMessage);
    }

    private static void validateBusinessRules(Money money, TransactionType type) {
        if (type == TransactionType.DEBIT && money.amount().compareTo(MAX_DEBIT_AMOUNT) > 0) {
            throw new TransactionLimitExceededException("DEBIT transactions cannot exceed $" + MAX_DEBIT_AMOUNT + " MXN");
        }
    }

    /**
     * Moves a pending transaction to the executed state and records its provider details.
     *
     * @param result approved result from the provider
     * @throws InvalidTransactionStateException when the transaction is not pending
     * @throws IllegalArgumentException when the provider result is not approved
     */
    public void markAsExecuted(ProviderTransactionResult result) {
        ensurePending("EXECUTED");
        if (result.outcome() != ProviderTransactionOutcome.APPROVED) {
            throw new IllegalArgumentException("An approved provider result is required");
        }
        this.status = TransactionStatus.EXECUTED;
        this.providerTransactionId = result.providerTransactionId();
        this.balanceAfter = result.balanceAfter();
        this.executedAt = result.executedAt();
        domainEvents.add(new TransactionExecutedEvent(id, accountId.value(), Instant.now()));
    }

    /**
     * Moves a pending transaction to the rejected state and records the provider response.
     *
     * @param result rejected result from the provider
     * @throws InvalidTransactionStateException when the transaction is not pending
     * @throws IllegalArgumentException when the provider result is not rejected
     */
    public void markAsRejected(ProviderTransactionResult result) {
        ensurePending("REJECTED");
        if (result.outcome() != ProviderTransactionOutcome.REJECTED) {
            throw new IllegalArgumentException("A rejected provider result is required");
        }
        this.status = TransactionStatus.REJECTED;
        this.providerTransactionId = result.providerTransactionId();
        this.balanceAfter = result.balanceAfter();
        this.executedAt = result.executedAt();
        this.errorCode = result.errorCode();
        this.errorMessage = result.errorMessage();
        domainEvents.add(new TransactionRejectedEvent(id, accountId.value(), Instant.now()));
    }

    /**
     * Marks a pending transaction as failed when execution could not be confirmed.
     *
     * @param result failed provider result
     * @throws InvalidTransactionStateException when the transaction is not pending
     * @throws IllegalArgumentException when the provider result is not failed
     */
    public void markAsFailed(ProviderTransactionResult result) {
        ensurePending("FAILED");
        if (result.outcome() != ProviderTransactionOutcome.FAILED) {
            throw new IllegalArgumentException("A failed provider result is required");
        }
        this.status = TransactionStatus.FAILED;
        this.errorCode = result.errorCode();
        this.errorMessage = result.errorMessage();
    }

    private void ensurePending(String targetStatus) {
        if (this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Only PENDING transactions can be " + targetStatus);
        }
    }

    /**
     * Returns recorded domain events and clears the internal list.
     *
     * @return immutable snapshot of the events recorded so far
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    /**
     * Returns an unmodifiable view of the currently recorded domain events.
     *
     * @return unmodifiable view of recorded domain events
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Returns the service-generated transaction identifier.
     *
     * @return transaction identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the account associated with the transaction.
     *
     * @return account identifier
     */
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Returns the transaction amount and currency.
     *
     * @return transaction money
     */
    public Money getMoney() {
        return money;
    }

    /**
     * Returns the transaction direction.
     *
     * @return transaction direction
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Returns the current transaction lifecycle status.
     *
     * @return current transaction status
     */
    public TransactionStatus getStatus() {
        return status;
    }

    /**
     * Returns the time the service created the transaction.
     *
     * @return transaction creation time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the optional description provided by the client.
     *
     * @return transaction description, or {@code null} if absent
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the provider transaction identifier, if available.
     *
     * @return provider transaction identifier, or {@code null} if unavailable
     */
    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    /**
     * Returns the resulting account balance reported by the provider, if available.
     *
     * @return resulting balance, or {@code null} if unavailable
     */
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    /**
     * Returns the execution time reported by the provider, if available.
     *
     * @return provider execution time, or {@code null} if unavailable
     */
    public Instant getExecutedAt() {
        return executedAt;
    }

    /**
     * Returns the provider or service error code, if applicable.
     *
     * @return error code, or {@code null} if there was no error
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the provider or service error detail, if applicable.
     *
     * @return error detail, or {@code null} if there was no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
