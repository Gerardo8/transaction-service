package com.spin.transactions.infrastructure.adapter.out.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for transaction rows and filtered transaction searches.
 */
public interface TransactionEntityRepository
        extends CrudRepository<TransactionEntity, UUID>, TransactionEntitySearchRepository {

    /**
     * Looks up the unique request key used to make transaction submission idempotent.
     *
     * @param idempotencyKey client request key
     * @return matching persisted transaction row
     */
    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);
}
