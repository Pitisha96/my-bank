package com.pitisha.project.mybank.transactionservice.infrastructure.client.exception;

public class AccountServiceException extends RuntimeException {

    public AccountServiceException(final String message) {
        super(message);
    }
}
