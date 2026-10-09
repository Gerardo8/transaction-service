package com.spin.transactions.infrastructure.adapter.in.web;

import com.spin.transactions.application.port.in.ExecuteTransactionUseCase;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionRequest;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionRequestMapper;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Inbound HTTP adapter for executing credit and debit transactions.
 */
@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Execute and search financial transactions")
public class ExecuteTransactionSpringMvcAdapter {

    private final ExecuteTransactionUseCase executeTransactionUseCase;

    /**
     * Creates the HTTP adapter with its application use case.
     *
     * @param executeTransactionUseCase transaction execution use case
     */
    public ExecuteTransactionSpringMvcAdapter(ExecuteTransactionUseCase executeTransactionUseCase) {
        this.executeTransactionUseCase = executeTransactionUseCase;
    }

    /**
     * Validates and submits a transaction, returning the outcome recorded by the provider.
     *
     * @param idempotencyKey client-generated key reused when retrying the same request
     * @param request validated transaction details
     * @return the persisted transaction and an outcome-specific HTTP status
     */
    @Operation(
            summary = "Execute a transaction",
            description = "Validates business rules before calling the provider and persists the provider outcome.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credit or debit transaction to execute",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransactionRequest.class))))
    @ApiResponse(responseCode = "201", description = "Transaction approved and persisted",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "202", description = "Transaction is still pending",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "422", description = "Business rule violation or provider rejection",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "503", description = "Provider outcome failed or is unavailable",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @PostMapping
    public ResponseEntity<TransactionResponse> executeTransaction(
            @Parameter(
                    name = "Idempotency-Key",
                    description = "Unique key for this operation; reuse it for retries of the same request",
                    in = ParameterIn.HEADER,
                    required = true,
                    example = "transfer-001")
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request) {

        final var transactionCommand = TransactionRequestMapper.toCommand(request, idempotencyKey);
        final var transaction = this.executeTransactionUseCase.execute(transactionCommand);

        final var body = TransactionResponseMapper.toResponse(transaction);

        return switch (transaction.getStatus()) {
            case EXECUTED -> ResponseEntity.status(HttpStatus.CREATED).body(body);
            case REJECTED -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(body);
            case FAILED -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
            case PENDING -> ResponseEntity.accepted().body(body);
        };
    }
}
