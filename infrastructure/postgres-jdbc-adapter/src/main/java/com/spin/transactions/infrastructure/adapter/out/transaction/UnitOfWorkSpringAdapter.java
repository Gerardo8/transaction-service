package com.spin.transactions.infrastructure.adapter.out.transaction;

import com.spin.transactions.application.port.out.UnitOfWorkPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Implements the application unit-of-work port using Spring's transaction template.
 */
@Component
public class UnitOfWorkSpringAdapter implements UnitOfWorkPort {

    private final TransactionTemplate transactionTemplate;

    /**
     * Creates the adapter using the transaction manager configured for the datasource.
     *
     * @param transactionTemplate Spring transaction template
     */
    public UnitOfWorkSpringAdapter(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Executes the supplied work in one database transaction.
     *
     * @param work transactional operation
     * @param <T> operation result type
     * @return result returned by the operation
     */
    @Override
    public <T> T execute(Supplier<T> work) {
        return this.transactionTemplate.execute(status -> work.get());
    }
}
