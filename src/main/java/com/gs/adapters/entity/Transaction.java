package com.gs.adapters.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
public class Transaction {
    @Id
    @Column(name = "transaction_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;
    @ManyToOne
    private Users studentDetails;
    @ManyToOne
    private Users supervisorDetails;
    @ManyToOne
    private Asset assetDetails;
    private Date loanDate;
    private String type;
    @ManyToOne
    private Users returnSupervisorDetails;
    private Date returnDate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Users getReturnSupervisorDetails() {
        return returnSupervisorDetails;
    }

    public void setReturnSupervisorDetails(Users returnSupervisorDetails) {
        this.returnSupervisorDetails = returnSupervisorDetails;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Users getSupervisorDetails() {
        return supervisorDetails;
    }

    public void setSupervisorDetails(Users supervisorDetails) {
        this.supervisorDetails = supervisorDetails;
    }

    public Asset getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(Asset assetDetails) {
        this.assetDetails = assetDetails;
    }

    public Users getStudentDetails() {
        return studentDetails;
    }

    public void setStudentDetails(Users studentDetails) {
        this.studentDetails = studentDetails;
    }

    public Date getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(Date loanDate) {
        this.loanDate = loanDate;
    }



}
