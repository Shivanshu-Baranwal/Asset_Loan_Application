package com.gs.adapters.grpc;

import com.gs.ports.serviceInterface.TransactionServices;
import com.gs.proto.*;
import com.gs.ports.model.DataMembers;
import io.quarkus.grpc.GrpcService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
@GrpcService
public class TransactionService extends MutinyLoanAssetGrpc.LoanAssetImplBase {

    TransactionServices transactionServices;
    @Inject
    public TransactionService(TransactionServices transactionServices) {
        this.transactionServices = transactionServices;
    }

    @Override
    @WithTransaction
    public Uni<CreateTransactionResponse> createTransaction(CreateTransactionRequest request) {
        return request.getType().equals(DataMembers.loanType)
                ?transactionServices.createLoanTransaction(request.getAssetId(),  request.getStudentId(),  request.getSupervisorId(), request.getLoanDate())
                :transactionServices.createReturnTransaction(request.getTransactionId(),  request.getReturnSupervisorId(), request.getReturnDate());
    }

    @Override
    @WithTransaction
    public Uni<APIResponse> updateTransaction(UpdateTransactionRequest request) {
        return request.getType().equals(DataMembers.loanType)
                ?transactionServices.updateLoanTransaction(request.getTransactionId(),  request.getNewObj().getAssetId(),  request.getNewObj().getStudentId(),  request.getNewObj().getSupervisorId(), request.getNewObj().getLoanDate())
                :transactionServices.updateReturnTransaction(request.getTransactionId(),  request.getNewObj().getReturnSupervisorId(), request.getNewObj().getReturnDate());
    }

    @Override
    @WithTransaction
    public Uni<Transaction> getTransactionById(FetchTransactionRequest request) {
        return transactionServices.getTransactionById(request.getTransactionId());
    }

    @Override
    @WithTransaction
    public Uni<APIResponse> deleteTransaction(FetchTransactionRequest request) {
        return request.getType().equals(DataMembers.loanType)
                ?transactionServices.deleteLoanTransaction(request.getTransactionId())
                :transactionServices.deleteReturnTransaction(request.getTransactionId());
    }

    @Override
    @WithTransaction
    public Uni<TransactionResponse> getAll(FetchAllTransactionRequest request) {
        return transactionServices.getAllTransaction( request.getUserId());
    }

    @Override
    @WithTransaction
    public Uni<TransactionResponse> filterTransactions(FilterTransactionRequest request) {
        return transactionServices.filterAllTransaction( request.getUserId(), request.getAssetId(), request.getStudentId(), request.getSupervisorId(), request.getDate());
    }
}
