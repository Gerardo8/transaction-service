package com.spin.transactions.infrastructure.adapter.in.web;

import com.spin.transactions.application.port.in.GetTransactionUseCase;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionResponseMapper;
import com.spin.transactions.infrastructure.adapter.in.web.dto.TransactionSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound HTTP adapter for searching persisted transactions.
 */
@RestController
@RequestMapping("/transactions")
@Validated
@Tag(name = "Transactions", description = "Execute and search financial transactions")
public class GetTransactionSpringMvcAdapter {

    private final GetTransactionUseCase getTransactionUseCase;

    /**
     * Creates the HTTP adapter with its transaction search use case.
     *
     * @param getTransactionUseCase transaction search use case
     */
    public GetTransactionSpringMvcAdapter(GetTransactionUseCase getTransactionUseCase) {
        this.getTransactionUseCase = getTransactionUseCase;
    }

    /**
     * Searches stored transaction outcomes using optional filters and bounded pagination.
     *
     * @param accountId optional exact account identifier
     * @param status optional transaction outcome filter
     * @param type optional CREDIT or DEBIT filter
     * @param page zero-based page index
     * @param limit number of results per page, from 1 through 100
     * @return a page of matching transactions and pagination metadata
     */
    @Operation(
            summary = "Search transactions",
            description = "Returns persisted transactions ordered from newest to oldest.")
    @ApiResponse(responseCode = "200", description = "Matching transactions",
            content = @Content(schema = @Schema(implementation = TransactionSearchResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameter")
    @GetMapping
    public ResponseEntity<TransactionSearchResponse> searchTransactions(
            @Parameter(description = "Exact account identifier", in = ParameterIn.QUERY, example = "acc-123456")
            @RequestParam(name = "accountId", required = false) String accountId,
            @Parameter(description = "Filter by transaction status")
            @RequestParam(name = "status", required = false) TransactionStatus status,
            @Parameter(description = "Filter by CREDIT or DEBIT transaction type")
            @RequestParam(name = "type", required = false) TransactionType type,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Maximum number of items per page (1-100)", example = "20")
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(TransactionResponseMapper.toSearchResponse(
                this.getTransactionUseCase.search(accountId, status, type, page, limit)));
    }
}
