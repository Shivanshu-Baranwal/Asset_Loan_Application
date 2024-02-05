package com.gs.ports.serviceInterface;

import com.gs.ports.model.TransactionException;
import com.gs.proto.APIResponse;
import com.gs.proto.CreateTransactionResponse;
import com.gs.proto.Transaction;
import com.gs.proto.TransactionResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface TransactionServices {

    Uni<CreateTransactionResponse> createLoanTransaction(String assetId, String studentId, String supervisorId, String loanDate);

    Uni<CreateTransactionResponse> createReturnTransaction(String transactionId, String returnSupervisorId, String returnDate);

    Uni<APIResponse> updateLoanTransaction(String transactionId, String assetId, String studentId, String supervisorId, String loanDate);

    Uni<APIResponse> updateReturnTransaction(String transactionId, String returnSupervisorId, String returnDate);

    Uni<Transaction> getTransactionById(String transactionId);

    Uni<APIResponse> deleteLoanTransaction(String  transactionId);

    Uni<APIResponse> deleteReturnTransaction(String  transactionId);

    Uni<TransactionResponse> getAllTransaction(String  userId);

    Uni<TransactionResponse> filterAllTransaction(String  userId, String assetId, String studentId, String supervisorId, String loanDate);
}
