package com.gs.adapters.repository;

import com.gs.adapters.entity.Transaction;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

}
