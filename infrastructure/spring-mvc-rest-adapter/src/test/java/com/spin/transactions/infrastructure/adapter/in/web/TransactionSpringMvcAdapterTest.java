package com.spin.transactions.infrastructure.adapter.in.web;

import com.spin.transactions.application.port.in.ExecuteTransactionUseCase;
import com.spin.transactions.application.port.in.GetTransactionUseCase;
import com.spin.transactions.application.command.TransactionCommand;
import com.spin.transactions.domain.exception.TransactionLimitExceededException;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import com.spin.transactions.domain.model.Transaction;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.infrastructure.adapter.in.web.ExecuteTransactionSpringMvcAdapter;
import com.spin.transactions.infrastructure.adapter.in.web.GetTransactionSpringMvcAdapter;
import com.spin.transactions.infrastructure.exception.GlobalExceptionHandler;
import com.spin.transactions.infrastructure.config.OpenApiConfiguration;
import com.spin.transactions.infrastructure.WebAdapterTestApplication;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(classes = WebAdapterTestApplication.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, ExecuteTransactionSpringMvcAdapter.class,
        GetTransactionSpringMvcAdapter.class, OpenApiConfiguration.class})
class TransactionSpringMvcAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecuteTransactionUseCase executeTransactionUseCase;

    @MockitoBean
    private GetTransactionUseCase getTransactionUseCase;

    @Test
    void executeReturnsTransaction() throws Exception {
        Transaction executed = TransactionFixtures.anExecutedTransaction();
        when(executeTransactionUseCase.execute(any())).thenReturn(executed);

        await(post("/transactions")
                        .header("Idempotency-Key", "idempotency-key-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-002",
                                  "amount": 1000.00,
                                  "currency": "MXN",
                                  "type": "CREDIT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(executed.getId().toString()))
                .andExpect(jsonPath("$.status").value("EXECUTED"))
                .andExpect(jsonPath("$.description").value("Test credit"));
        var command = ArgumentCaptor.forClass(TransactionCommand.class);
        verify(executeTransactionUseCase).execute(command.capture());
        assertEquals("idempotency-key-001", command.getValue().idempotencyKey());
    }

    @Test
    void executeMapsLimitExceededTo422() throws Exception {
        when(executeTransactionUseCase.execute(any())).thenThrow(
                new TransactionLimitExceededException("DEBIT transactions cannot exceed $10000.00 MXN"));

        await(post("/transactions")
                        .header("Idempotency-Key", "idempotency-key-003")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-001",
                                  "amount": 15000.00,
                                  "currency": "MXN",
                                  "type": "DEBIT"
                                }
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void executeReturnsPersistedProviderRejectionAs422() throws Exception {
        Transaction rejected = TransactionFixtures.aRejectedTransaction();
        when(executeTransactionUseCase.execute(any())).thenReturn(rejected);

        await(post("/transactions")
                        .header("Idempotency-Key", "idempotency-key-rejected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-003",
                                  "amount": 150.00,
                                  "currency": "MXN",
                                  "type": "DEBIT",
                                  "description": "Transfer"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.errorMessage").value("Insufficient funds"));
    }

    @Test
    void executeReturnsStoredFailureAsServiceUnavailable() throws Exception {
        Transaction failed = TransactionFixtures.aFailedTransaction();
        when(executeTransactionUseCase.execute(any())).thenReturn(failed);

        await(post("/transactions")
                        .header("Idempotency-Key", "idempotency-key-failed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-004",
                                  "amount": 300.00,
                                  "currency": "MXN",
                                  "type": "CREDIT",
                                  "description": "Transfer"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("PROVIDER_TIMEOUT"));
    }

    @Test
    void executeRejectsAmountAtOrBelowOneBeforeCallingUseCase() throws Exception {
        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", "idempotency-key-small-amount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "ACC-001",
                                  "amount": 1.00,
                                  "currency": "MXN",
                                  "type": "CREDIT"
                                }
                                """))
                .andExpect(status().isBadRequest());
        verify(executeTransactionUseCase, never()).execute(any());
    }

    @Test
    void searchReturnsPageMetadataAndFilters() throws Exception {
        Transaction executed = TransactionFixtures.anExecutedTransaction();
        when(getTransactionUseCase.search("ACC-002", executed.getStatus(), executed.getType(), 0, 20))
                .thenReturn(new TransactionPage(List.of(executed), 0, 20, 1));

        await(get("/transactions")
                        .queryParam("accountId", "ACC-002")
                        .queryParam("status", "EXECUTED")
                        .queryParam("type", "CREDIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(executed.getId().toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void searchRejectsInvalidLimit() throws Exception {
        mockMvc.perform(get("/transactions").queryParam("limit", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void openApiYamlDocumentsTransactionEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("openapi:")))
                .andExpect(content().string(containsString("/transactions:")))
                .andExpect(content().string(containsString("Execute a transaction")))
                .andExpect(content().string(containsString("Search transactions")));
    }

    private ResultActions await(RequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }
}
