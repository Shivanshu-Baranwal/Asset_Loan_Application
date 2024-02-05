package com.gs.adapters.database;

import com.gs.adapters.repository.TransactionRepository;
import com.gs.adapters.entity.Transaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class TransactionDatabaseServices {
    TransactionRepository transactionRepository;

    @Inject
    public TransactionDatabaseServices(TransactionRepository transactionRepository)
    {
        this.transactionRepository = transactionRepository;
    }

    public Uni<Transaction> persistTransaction(Transaction obj)
    {
        return transactionRepository.persist(obj);
    }

    public Uni<Long> deleteTransaction(UUID id)
    {
        return transactionRepository.delete("id=?1", id);
    }
}
