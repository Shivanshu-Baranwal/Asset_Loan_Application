package com.gs.ports.model;

public class TransactionException extends RuntimeException{
    public TransactionException(String message)
    {
        super(message);
    }
}
