package com.gs.ports.domainservice;

import com.gs.adapters.database.*;
import com.gs.adapters.repository.AssetRepository;
import com.gs.adapters.repository.TransactionRepository;
import com.gs.ports.serviceInterface.TransactionServices;
import com.gs.proto.APIResponse;
import com.gs.proto.CreateTransactionResponse;
import com.gs.proto.Transaction;
import com.gs.proto.TransactionResponse;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.gs.ports.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionServiceImpl implements TransactionServices {
    TransactionRepository transactionRepository;
    TransactionDatabaseRepository transactionDatabaseRepository;
    AssetDatabaseRepository assetDatabaseRepository;
    AssetRepository assetRepository;
    UserDatabaseRepository userDatabaseRepository;
    AssetDatabaseService assetDatabaseService;
    TransactionDatabaseServices transactionDatabaseServices;

    @Inject
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionDatabaseRepository transactionDatabaseRepository, AssetDatabaseRepository assetDatabaseRepository, AssetRepository assetRepository, UserDatabaseRepository userDatabaseRepository, AssetDatabaseService assetDatabaseService, TransactionDatabaseServices transactionDatabaseServices)
    {
        this.transactionRepository = transactionRepository;
        this.transactionDatabaseRepository = transactionDatabaseRepository;
        this.assetDatabaseRepository = assetDatabaseRepository;
        this.assetRepository = assetRepository;
        this.userDatabaseRepository = userDatabaseRepository;
        this.assetDatabaseService = assetDatabaseService;
        this.transactionDatabaseServices = transactionDatabaseServices;
    }

    public void checkArgument(String id, String value)
    {
        if(id.isEmpty() || id.split("-").length != 5 || id.charAt(id.length() - 1) >= 'g')
        {
            throw new IllegalArgumentException("Invalid" + value + "Id Format");
        }
    }
    
    @Override
    public Uni<CreateTransactionResponse> createLoanTransaction(String assetId, String studentId, String supervisorId, String loanDate) {
        com.gs.adapters.entity.Transaction newTransaction = new com.gs.adapters.entity.Transaction();
        this.checkArgument(assetId, DataMembers.assetValue);
        this.checkArgument(studentId, DataMembers.studentValue);
        this.checkArgument(supervisorId, DataMembers.supervisorValue);
        return userDatabaseRepository.findUserByRole(UUID.fromString(studentId), DataMembers.studentValue)
                .chain(studentDataModel -> {
                    newTransaction.setStudentDetails(studentDataModel);
                    return userDatabaseRepository.findUserByRole(UUID.fromString(supervisorId), DataMembers.supervisorValue)
                            .chain(supervisorDataModel-> {
                                newTransaction.setSupervisorDetails(supervisorDataModel);
                                return assetDatabaseRepository.findAsset(UUID.fromString(assetId), false)
                                        .chain(assetDataModel -> {
                                            assetDataModel.setOccupied(true);
                                            return assetDatabaseService.updateAsset(assetDataModel).chain(isPer -> {
                                                if (!assetRepository.isPersistent(isPer)) {
                                                    throw new TransactionException("Failed to persist assetDataModel");
                                                }
                                                newTransaction.setAssetDetails(assetDataModel);
                                                try {
                                                    newTransaction.setLoanDate(new SimpleDateFormat(DataMembers.dateFormat).parse(loanDate));
                                                }
                                                catch (ParseException e)
                                                {
                                                    throw new IllegalArgumentException(DataMembers.invalidDate);
                                                }
                                                newTransaction.setType(DataMembers.loanType);
                                                return transactionDatabaseServices.persistTransaction(newTransaction).chain(isPeri->{
                                                    if(!transactionRepository.isPersistent(isPeri))
                                                    {
                                                        throw new TransactionException("Failed to Persist");
                                                    }
                                                    return Uni.createFrom().item(()-> CreateTransactionResponse.newBuilder()
                                                            .setResponseCode(200)
                                                            .setResponseMessage("Created")
                                                            .setTransactionId(newTransaction.getTransactionId().toString())
                                                            .build());
                                                });

                                            });
                                        });
                            });
                });
    }

    @Override
    public Uni<CreateTransactionResponse> createReturnTransaction(String transactionId, String returnSupervisorId, String returnDate) {
        this.checkArgument(transactionId, DataMembers.transactionValue);
        this.checkArgument(returnSupervisorId, DataMembers.supervisorValue);
        return transactionDatabaseRepository.findTransactionById(UUID.fromString(transactionId))
                .chain(returnDataModel -> {
                    Date date;
                    try{
                        date = new SimpleDateFormat(DataMembers.dateFormat).parse(returnDate);
                    }
                    catch (Exception e)
                    {
                        throw new IllegalArgumentException(DataMembers.invalidDate);
                    }
                    if(returnDataModel.getType().equals(DataMembers.returnType))
                    {
                        throw new TransactionException("Already Returned");
                    }
                    returnDataModel.setReturnDate(date);
                    return assetDatabaseRepository.findAssetById(returnDataModel.getAssetDetails().getAssetId())
                            .chain(assetDataModel->{
                                assetDataModel.setOccupied(false);
                                return assetDatabaseService.updateAsset(assetDataModel).chain(isPer->{
                                    if(!assetRepository.isPersistent(isPer))
                                    {
                                        throw new TransactionException("Failed to Persist Asset");
                                    }
                                    return userDatabaseRepository.findUserByRole(UUID.fromString(returnSupervisorId), DataMembers.supervisorValue)
                                            .chain(userDataModel->{
                                                returnDataModel.setReturnSupervisorDetails(userDataModel);
                                                returnDataModel.setType(DataMembers.returnType);
                                                return transactionDatabaseServices.persistTransaction(returnDataModel).chain(isPersi->{
                                                    if(!transactionRepository.isPersistent(isPersi))
                                                    {
                                                        throw new TransactionException(DataMembers.failedPersistTransaction);
                                                    }
                                                    return Uni.createFrom().item(()->CreateTransactionResponse.newBuilder()
                                                            .setResponseCode(201)
                                                            .setResponseMessage("Returned Successfully")
                                                            .setTransactionId(transactionId)
                                                            .build()
                                                    );
                                                });
                                            });
                                });
                            });
                });
    }

    @Override
    public Uni<APIResponse> updateLoanTransaction(String transactionId, String assetId, String studentId, String supervisorId, String loanDate) {
        this.checkArgument(assetId, DataMembers.assetValue);
        this.checkArgument(studentId, DataMembers.studentValue);
        this.checkArgument(supervisorId, DataMembers.supervisorValue);
        this.checkArgument(transactionId, DataMembers.transactionValue);
        return transactionDatabaseRepository.findTransactionById(UUID.fromString(transactionId))
                .chain(transactionDataModel->{
                    Date date;
                    try{
                        date = new SimpleDateFormat(DataMembers.dateFormat).parse(loanDate);
                    }
                    catch (Exception e)
                    {
                        throw new IllegalArgumentException(DataMembers.invalidDate);
                    }
                    if(!date.equals(transactionDataModel.getLoanDate())) {
                        transactionDataModel.setLoanDate(date);
                    }
                    return userDatabaseRepository.findUserByRole(UUID.fromString(studentId), DataMembers.studentValue)
                            .chain(studentDataModel->{
                                if(!transactionDataModel.getStudentDetails().getUserId().equals(UUID.fromString(studentId)))
                                {
                                    transactionDataModel.setStudentDetails(studentDataModel);
                                }
                                return userDatabaseRepository.findUserByRole(UUID.fromString(supervisorId), DataMembers.supervisorValue)
                                        .chain(supervisorDataModel->{
                                            if(!transactionDataModel.getSupervisorDetails().getUserId().equals(UUID.fromString(studentId)))
                                            {
                                                transactionDataModel.setSupervisorDetails(supervisorDataModel);
                                            }
                                            if(!transactionDataModel.getAssetDetails().getAssetId().equals(UUID.fromString(assetId)))
                                            {
                                                return assetDatabaseRepository.findAsset(UUID.fromString(assetId), false)
                                                        .chain(assetDataModel->{
                                                            assetDataModel.setOccupied(true);
                                                            return assetDatabaseRepository.findAssetById(transactionDataModel.getAssetDetails().getAssetId())
                                                                    .chain(oldAsset->{
                                                                        oldAsset.setOccupied(false);
                                                                        return assetDatabaseService.updateAsset(oldAsset).chain(isPer->{
                                                                            if(!assetRepository.isPersistent(isPer))
                                                                            {
                                                                                throw new TransactionException("Failed to persist Asset");
                                                                            }
                                                                            return assetDatabaseService.updateAsset(assetDataModel).chain(isPersi->{
                                                                                if(!assetRepository.isPersistent(isPersi))
                                                                                {
                                                                                    throw new TransactionException("Failed to persist new Asset");
                                                                                }
                                                                                transactionDataModel.setAssetDetails(assetDataModel);
                                                                                return transactionDatabaseServices.persistTransaction(transactionDataModel).chain(isPersis->{
                                                                                    if(!transactionRepository.isPersistent(isPersis))
                                                                                    {
                                                                                        throw new TransactionException(DataMembers.failedPersistTransaction);
                                                                                    }
                                                                                    return Uni.createFrom().item(()->APIResponse.newBuilder()
                                                                                            .setResponseMessage("Successfully Updated")
                                                                                            .setResponseCode(202)
                                                                                            .build());
                                                                                });
                                                                            });
                                                                        });
                                                                    });
                                                        });
                                            }
                                            else
                                            {
                                                return transactionDatabaseServices.persistTransaction(transactionDataModel).chain(isPersis->{
                                                    if(!transactionRepository.isPersistent(isPersis))
                                                    {
                                                        throw new TransactionException(DataMembers.failedPersistTransaction);
                                                    }
                                                    return Uni.createFrom().item(()->APIResponse.newBuilder()
                                                            .setResponseMessage("Successfully Updated")
                                                            .setResponseCode(202)
                                                            .build());
                                                });
                                            }
                                        });
                            });

                });
    }

    @Override
    public Uni<APIResponse> updateReturnTransaction(String transactionId, String returnSupervisorId, String returnDate) {
        this.checkArgument(transactionId, DataMembers.transactionValue);
        this.checkArgument(returnSupervisorId, DataMembers.supervisorValue);
        return transactionDatabaseRepository.findTransactionById(UUID.fromString(transactionId))
                .chain(transactionDataModel->{
                    if(!transactionDataModel.getType().equals(DataMembers.returnType))
                    {
                        throw new TransactionException("Asset isn't returned yet!..");
                    }
                    Date date;
                    try{
                        date = new SimpleDateFormat(DataMembers.dateFormat).parse(returnDate);
                    }
                    catch (Exception e)
                    {
                        throw new IllegalArgumentException(DataMembers.invalidDate);
                    }
                    if(!date.equals(transactionDataModel.getReturnDate())) {
                        transactionDataModel.setReturnDate(date);
                    }

                    return userDatabaseRepository.findUserByRole(UUID.fromString(returnSupervisorId), DataMembers.supervisorValue)
                            .chain(userDataModel-> {
                                if (!transactionDataModel.getSupervisorDetails().getUserId().equals(UUID.fromString(returnSupervisorId))) {
                                    transactionDataModel.setReturnSupervisorDetails(userDataModel);
                                }
                                return transactionDatabaseServices.persistTransaction(transactionDataModel).chain(isPer -> {
                                    if (!transactionRepository.isPersistent(isPer)) {
                                        throw new TransactionException(DataMembers.failedPersistTransaction);
                                    }
                                    return Uni.createFrom().item(()->APIResponse.newBuilder()
                                            .setResponseMessage("Updated Successfully")
                                            .setResponseCode(202)
                                            .build());

                                });
                            });
                });
    }

    @Override
    public Uni<Transaction> getTransactionById(String transactionId) {
        this.checkArgument(transactionId, DataMembers.transactionValue);
        return transactionDatabaseRepository.findTransactionById(UUID.fromString(transactionId))
                .chain(transactionDataModel->
                        Uni.createFrom().item(()->Transaction.newBuilder()
                                    .setTransactionId(transactionDataModel.getTransactionId().toString())
                                    .setStudentName(transactionDataModel.getStudentDetails().getFirstName() + ' ' +transactionDataModel.getStudentDetails().getLastName())
                                    .setSupervisorName(transactionDataModel.getSupervisorDetails().getFirstName() + ' ' +transactionDataModel.getSupervisorDetails().getLastName())
                                    .setAssetName(transactionDataModel.getAssetDetails().getName())
                                    .setAssetId(transactionDataModel.getAssetDetails().getAssetId().toString())
                                    .setTransactionType(transactionDataModel.getType())
                                    .setLoanDate(transactionDataModel.getLoanDate().toString())
                                    .setReturningSupervisorName(transactionDataModel.getReturnSupervisorDetails() != null?transactionDataModel.getReturnSupervisorDetails().getFirstName() + " " + transactionDataModel.getReturnSupervisorDetails().getLastName():"")
                                    .setReturnDate(transactionDataModel.getReturnDate() != null ? transactionDataModel.getReturnDate().toString() : "")
                                    .build())

                );
    }

    @Override
    public Uni<APIResponse> deleteLoanTransaction(String transactionId) {
        this.checkArgument(transactionId, DataMembers.transactionValue);
        return transactionDatabaseRepository.findTransactionById(UUID.fromString(transactionId))
                .chain(transaction -> 
                    //Updating Asset
                    assetDatabaseRepository.findAssetById(transaction.getAssetDetails().getAssetId())
                            .chain(oldAsset-> {
                                if(transaction.getReturnDate() != null)
                                {
                                    throw new TransactionException("Delete Return First");
                                }
                                oldAsset.setOccupied(false);
                                return assetDatabaseService.updateAsset(oldAsset).chain(isPer -> {
                                    if (!assetRepository.isPersistent(isPer)) {
                                        throw new TransactionException("Failed to persist Asset");
                                    }
                                    return transactionDatabaseServices.deleteTransaction(transaction.getTransactionId()).chain(isDel->
                                        //Returning Response
                                        Uni.createFrom().item(()->APIResponse.newBuilder()
                                                .setResponseMessage("Deleted Successfully")
                                                .setResponseCode(202)
                                                .build())
                                    );
                                });
                            })
                );
    }

    @Override
    public Uni<APIResponse> deleteReturnTransaction(String transactionId) {
        this.checkArgument(transactionId, DataMembers.transactionValue);
        return transactionDatabaseRepository.findTransactionById(UUID.fromString(transactionId))
                .chain(transaction -> {
                    if(!transaction.getType().equals(DataMembers.returnType))
                    {
                        throw new TransactionException("Asset isn't returned yet!..");
                    }

                    return assetDatabaseRepository.findAsset(transaction.getAssetDetails().getAssetId(), false)
                            .chain(assetDataModel->{
                                assetDataModel.setOccupied(true);
                                return assetDatabaseService.updateAsset(assetDataModel).chain(isPer->{
                                    if(!assetRepository.isPersistent(isPer))
                                    {
                                        throw new TransactionException("Failed to persist");
                                    }
                                    //Deleting return transaction
                                    transaction.setReturnSupervisorDetails(null);
                                    transaction.setReturnDate(null);
                                    transaction.setType(DataMembers.loanType);
                                    return transactionDatabaseServices.persistTransaction(transaction).chain(isPersi->{
                                        if(!transactionRepository.isPersistent(isPersi))
                                        {
                                            throw new TransactionException(DataMembers.failedPersistTransaction);
                                        }
                                        return Uni.createFrom().item(()->APIResponse.newBuilder()
                                                .setResponseMessage("Deleted Returned")
                                                .setResponseCode(202)
                                                .build());
                                    });
                                });
                            });
                });
    }

    @Override
    public Uni<TransactionResponse> getAllTransaction(String userId) {
        this.checkArgument(userId, DataMembers.userValue);
        return userDatabaseRepository.findUser(UUID.fromString(userId))
                .chain(userDataModel->{
                    if(userDataModel.getRole().equals(DataMembers.supervisorValue))
                    {
                        return getResponse(transactionRepository.listAll());
                    }
                    else {
                        return getResponse(transactionDatabaseRepository.listTransaction("studentDetails.userId = :userId", Parameters.with("userId", userDataModel.getUserId())));

                    }
                });
    }

    public static String queryGenerator(String assetId, String studentId, String supervisorId, String loanDate)
    {
        String queryParameter = "";
        queryParameter += !assetId.isEmpty() ? "assetDetails.assetId = :assetId" : "";

        queryParameter += (!studentId.isEmpty() && !queryParameter.isEmpty()) ? " and  " : "";
        queryParameter += !studentId.isEmpty() ? "studentDetails.userId = :studentId" : "";

        queryParameter += (!supervisorId.isEmpty() && !queryParameter.isEmpty())?" and ":"";
        queryParameter += !supervisorId.isEmpty() ? "supervisorDetails.userId = :supervisorId" : "";

        queryParameter += (!loanDate.isEmpty() && !queryParameter.isEmpty())?"  and ":"";
        queryParameter += !loanDate.isEmpty() ? "loanDate = :loanDate" : "";

        return queryParameter;
    }

    Parameters getParameter(String assetId, String studentId, String supervisorId, String loanDate)
    {
        Parameters parameters = new Parameters();

        if(!assetId.isEmpty())
        {
            if(assetId.split("-").length != 5 || assetId.charAt(assetId.length() - 1) >= 'g')
            {
                throw new IllegalArgumentException("Invalid Asset Id");
            }
            parameters.and("assetId", UUID.fromString(assetId));
        }

        if(!studentId.isEmpty())
        {
            if(studentId.split("-").length != 5 || studentId.charAt(studentId.length() - 1) >= 'g')
            {
                throw new IllegalArgumentException("Invalid student Id");
            }
            parameters.and("studentId", UUID.fromString(studentId));
        }

        if(!supervisorId.isEmpty())
        {
            if(supervisorId.split("-").length != 5 || supervisorId.charAt(supervisorId.length() - 1) >= 'g')
            {
                throw new IllegalArgumentException("Invalid Supervisor Id");
            }
            parameters.and("supervisorId", UUID.fromString(supervisorId));
        }

        if(!(loanDate.isEmpty()))
        {
            try {
                parameters.and("loanDate", new SimpleDateFormat(DataMembers.dateFormat).parse(loanDate));
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException(DataMembers.invalidDate);
            }
        }

        return parameters;
    }
    @Override
    public Uni<TransactionResponse> filterAllTransaction(String userId, String assetId, String studentId, String supervisorId, String loanDate) {
        this.checkArgument(userId, DataMembers.userValue);

        if(queryGenerator(assetId, studentId, supervisorId, loanDate).isEmpty())
        {
            return this.getAllTransaction(userId);
        }
        return userDatabaseRepository.findUser(UUID.fromString(userId))
                .chain(userDataModel->{
                    if(userDataModel.getRole().equals(DataMembers.supervisorValue))
                    {
                        return this.getResponse(transactionDatabaseRepository.listTransaction(queryGenerator(assetId, studentId, supervisorId, loanDate), getParameter(assetId, studentId, supervisorId, loanDate)));
                    }
                    else {
                        if(!studentId.equals(userId))
                        {
                            throw new TransactionException("Access Denied");
                        }
                        return this.getResponse(transactionDatabaseRepository.listTransaction("studentDetails.userId = :userId and " + queryGenerator(assetId, studentId, supervisorId, loanDate), getParameter(assetId, studentId, supervisorId, loanDate).and("userId", UUID.fromString(userId))));
                    }
                });
    }

    public Uni<TransactionResponse> getResponse(Uni<List<com.gs.adapters.entity.Transaction>> listTransaction)
    {
        return listTransaction.map(transactions -> {
            TransactionResponse.Builder responseBuilder = TransactionResponse.newBuilder();
            for (com.gs.adapters.entity.Transaction transaction : transactions) {
                responseBuilder.addTransaction(com.gs.proto.Transaction.newBuilder()
                        .setTransactionId(transaction.getTransactionId().toString())
                        .setTransactionType(transaction.getType())
                        .setAssetId(transaction.getAssetDetails().getAssetId().toString())
                        .setAssetName(transaction.getAssetDetails().getName())
                        .setStudentName(transaction.getStudentDetails().getFirstName() + " " + transaction.getStudentDetails().getLastName())
                        .setSupervisorName(transaction.getSupervisorDetails().getFirstName() + " " + transaction.getSupervisorDetails().getLastName())
                        .setReturningSupervisorName(transaction.getReturnSupervisorDetails() != null ? transaction.getReturnSupervisorDetails().getFirstName() + " " + transaction.getReturnSupervisorDetails().getLastName() : "")
                        .setLoanDate(transaction.getLoanDate().toString())
                        .setReturnDate(transaction.getReturnDate() != null ? transaction.getReturnDate().toString() : "")
                );
            }
            return responseBuilder.build();
        });
    }
}

