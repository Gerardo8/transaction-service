package com.spin.transactions.config;

import com.spin.transactions.application.port.out.DomainEventPublisherPort;
import com.spin.transactions.application.port.out.ExternalProviderPort;
import com.spin.transactions.application.port.out.TransactionRepositoryPort;
import com.spin.transactions.application.port.out.UnitOfWorkPort;
import com.spin.transactions.application.port.in.ExecuteTransactionUseCase;
import com.spin.transactions.application.service.ExecuteTransactionService;
import com.spin.transactions.application.port.in.GetTransactionUseCase;
import com.spin.transactions.application.service.GetTransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composes application use cases with the outbound ports supplied by infrastructure adapters.
 */
@Configuration
public class UseCaseConfiguration {

    /** Creates the application use-case configuration. */
    public UseCaseConfiguration() {
    }

    /**
     * Creates the transaction execution use case.
     *
     * @param transactionRepository transaction persistence port
     * @param externalProviderPort provider execution port
     * @param domainEventPublisher domain event publisher
     * @param unitOfWork persistence transaction boundary
     * @return configured transaction execution use case
     */
    @Bean
    ExecuteTransactionUseCase executeTransactionUseCase(
            TransactionRepositoryPort transactionRepository,
            ExternalProviderPort externalProviderPort,
            DomainEventPublisherPort domainEventPublisher,
            UnitOfWorkPort unitOfWork) {
        return new ExecuteTransactionService(
                transactionRepository, externalProviderPort, domainEventPublisher, unitOfWork);
    }

    /**
     * Creates the transaction search use case.
     *
     * @param transactionRepository transaction persistence port
     * @return configured transaction query use case
     */
    @Bean
    GetTransactionUseCase getTransactionUseCase(TransactionRepositoryPort transactionRepository) {
        return new GetTransactionService(transactionRepository);
    }
}
