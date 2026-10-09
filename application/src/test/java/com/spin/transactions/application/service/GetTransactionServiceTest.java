package com.spin.transactions.application.service;

import com.spin.transactions.application.port.out.TransactionRepositoryPort;
import com.spin.transactions.domain.fixtures.TransactionFixtures;
import com.spin.transactions.domain.model.TransactionPage;
import com.spin.transactions.domain.model.TransactionStatus;
import com.spin.transactions.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransactionServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;

    private GetTransactionService service;

    @BeforeEach
    void setUp() {
        service = new GetTransactionService(transactionRepository);
    }

    @Test
    void searchesTransactionsWithPagination() {
        var existing = TransactionFixtures.anExecutedTransaction();
        TransactionPage expected = new TransactionPage(List.of(existing), 1, 10, 11);
        when(transactionRepository.search("ACC-002", TransactionStatus.EXECUTED,
                TransactionType.CREDIT, 1, 10)).thenReturn(expected);

        assertEquals(expected, service.search("ACC-002", TransactionStatus.EXECUTED,
                TransactionType.CREDIT, 1, 10));
    }

    @Test
    void rejectsOutOfRangePageSize() {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, 0, 101));
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null, -1, 10));
    }
}
