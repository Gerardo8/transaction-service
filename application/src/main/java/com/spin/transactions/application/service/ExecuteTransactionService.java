package com.spin.transactions.application.service;

import com.spin.transactions.application.command.TransactionCommand;
import com.spin.transactions.application.port.in.ExecuteTransactionUseCase;
import com.spin.transactions.application.port.out.DomainEventPublisherPort;
import com.spin.transactions.application.port.out.ExternalProviderPort;
import com.spin.transactions.application.port.out.TransactionRepositoryPort;
import com.spin.transactions.application.port.out.UnitOfWorkPort;
import com.spin.transactions.domain.exception.ProviderUnavailableException;
import com.spin.transactions.domain.model.AccountId;
import com.spin.transactions.domain.model.Money;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionType;

import java.util.Currency;
import java.util.Locale;

/**
 * Orchestrates transaction execution, including idempotency, provider communication,
 * state persistence, and post-persistence event publication.
 */
public class ExecuteTransactionService implements ExecuteTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final ExternalProviderPort externalProviderPort;
    private final DomainEventPublisherPort domainEventPublisher;
    private final UnitOfWorkPort unitOfWork;

    /**
     * Creates the use case with its required outbound ports.
     *
     * @param transactionRepository transaction persistence port
     * @param externalProviderPort external transaction provider port
     * @param domainEventPublisher publisher for resulting domain events
     * @param unitOfWork persistence transaction boundary
     */
    public ExecuteTransactionService(
            TransactionRepositoryPort transactionRepository,
            ExternalProviderPort externalProviderPort,
            DomainEventPublisherPort domainEventPublisher,
            UnitOfWorkPort unitOfWork) {
        this.transactionRepository = transactionRepository;
        this.externalProviderPort = externalProviderPort;
        this.domainEventPublisher = domainEventPublisher;
        this.unitOfWork = unitOfWork;
    }

    /**
     * Returns an existing idempotent result or creates and executes a new transaction.
     *
     * @param command requested transaction and client idempotency key
     * @return persisted transaction outcome
     */
    @Override
    public Transaction execute(TransactionCommand command) {

        final var existingTransaction = this.transactionRepository
                .findByIdempotencyKey(command.idempotencyKey());

        return existingTransaction
                .orElseGet(() -> createAndProcess(command));
    }

    private Transaction createAndProcess(TransactionCommand command) {
        final var accountId = new AccountId(command.accountId());
        final var money = new Money(command.amount(), Currency.getInstance(command.currency()));
        final var type = TransactionType.valueOf(command.type().toUpperCase(Locale.ROOT));

        final var transaction = Transaction.create(accountId, money, type, command.description());
        final var saved = this.transactionRepository.save(transaction, command.idempotencyKey());
        ProviderTransactionResult result;
        try {
            result = this.externalProviderPort.processTransaction(saved, command.idempotencyKey());
        } catch (ProviderUnavailableException error) {
            result = ProviderTransactionResult.failed(
                    error.code(),
                    "Provider execution outcome is unknown; do not retry with a new idempotency key.");
        }
        switch (result.outcome()) {
            case APPROVED -> saved.markAsExecuted(result);
            case REJECTED -> saved.markAsRejected(result);
            case FAILED -> saved.markAsFailed(result);
        }
        final var persisted = this.unitOfWork.execute(
                () -> this.transactionRepository.save(saved, command.idempotencyKey()));
        this.domainEventPublisher.publish(persisted.pullDomainEvents());
        return persisted;
    }
}
