package com.gs.adapters.database;

import com.gs.adapters.repository.TransactionRepository;
import com.gs.adapters.entity.Transaction;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionDatabaseRepository {

    TransactionRepository transactionRepository;

    @Inject
    public TransactionDatabaseRepository(TransactionRepository transactionRepository)
    {
        this.transactionRepository = transactionRepository;
    }

    public Uni<Transaction> findTransactionById(UUID id) {
        return transactionRepository.find("id=?1", id)
                .firstResult()
                .onItem()
                .ifNull()
                .failWith(()->{
                    throw new RuntimeException("Transaction Id not found");
                });
    }

    public Uni<List<Transaction>> listTransaction(String query, Parameters parameters) {
        return transactionRepository.list(query, parameters);
    }

    public Uni<List<Transaction>> listAllTransaction()
    {
        return transactionRepository.listAll();
    }
}
