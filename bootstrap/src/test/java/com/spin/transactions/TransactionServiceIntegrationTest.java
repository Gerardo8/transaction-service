package com.spin.transactions;

import com.spin.transactions.application.port.out.ExternalProviderPort;
import com.spin.transactions.domain.model.ProviderTransactionResult;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.docker.compose.enabled=false",
                "external.provider.url=http://localhost:0"
        }
)
@Testcontainers
class TransactionServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Value("${local.server.port}")
    private int port;

    private RestClient restClient;

    @MockitoBean
    private ExternalProviderPort externalProviderPort;

    @BeforeEach
    void stubProvider() {
        restClient = RestClient.create("http://localhost:" + port);
        when(externalProviderPort.processTransaction(any(), any())).thenReturn(
                ProviderTransactionResult.approved(
                        "provider-e2e", new BigDecimal("1250.00"), Instant.parse("2025-03-15T10:30:00Z")));
    }

    @Test
    void executePersistsExecutedTransaction() {
        Map<String, Object> body = Map.of(
                "accountId", "ACC-E2E",
                "amount", 250.00,
                "currency", "MXN",
                "type", "CREDIT",
                "description", "Integration test"
        );

        ResponseEntity<TransactionResponse> created = restClient.post()
                .uri("/transactions")
                .header("Idempotency-Key", "e2e-idempotency-key")
                .body(body)
                .retrieve()
                .toEntity(TransactionResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        TransactionResponse response = created.getBody();
        assertNotNull(response);
        assertEquals(TransactionStatus.EXECUTED.name(), response.status());
        assertEquals("ACC-E2E", response.accountId());
        assertEquals("provider-e2e", response.providerTransactionId());
        assertEquals(new BigDecimal("1250.00"), new BigDecimal(response.balanceAfter().toString()));

        final ResponseEntity<TransactionSearchResponse> page = restClient.get()
                .uri("/transactions?accountId=ACC-E2E&status=EXECUTED&page=0&limit=5")
                .retrieve()
                .toEntity(TransactionSearchResponse.class);
        assertEquals(HttpStatus.OK, page.getStatusCode());
        assertEquals(1, Objects.requireNonNull(page.getBody()).totalElements());
        TransactionResponse first =  page.getBody().items().getFirst();
        assertEquals("provider-e2e", first.providerTransactionId());
    }
}
