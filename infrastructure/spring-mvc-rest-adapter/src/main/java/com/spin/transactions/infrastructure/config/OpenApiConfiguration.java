package com.spin.transactions.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supplies the service metadata used by springdoc when it generates OpenAPI documents.
 */
@Configuration
public class OpenApiConfiguration {

    /** Creates OpenAPI metadata configuration. */
    public OpenApiConfiguration() {
    }

    /**
     * Describes the transaction API in generated OpenAPI documents.
     *
     * @return OpenAPI metadata for the transaction service
     */
    @Bean
    OpenAPI transactionServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Transaction Execution API")
                .version("1.0.0")
                .description("""
                        Execute credit and debit transactions through an external provider and \
                        search persisted transaction outcomes.
                        """));
    }
}
