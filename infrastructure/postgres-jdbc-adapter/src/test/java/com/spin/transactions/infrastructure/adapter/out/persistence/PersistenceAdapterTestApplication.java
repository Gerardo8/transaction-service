package com.spin.transactions.infrastructure.adapter.out.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJdbcRepositories(basePackageClasses = TransactionEntityRepository.class)
class PersistenceAdapterTestApplication {
}
