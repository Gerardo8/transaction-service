package com.spin.transactions.application.port.out;

import java.util.function.Supplier;

/**
 * Outbound port for executing a short unit of work atomically.
 */
public interface UnitOfWorkPort {

    /**
     * Runs work within the persistence adapter's transaction boundary.
     *
     * @param work work to execute
     * @param <T> result type
     * @return the work's result
     */
    <T> T execute(Supplier<T> work);
}
