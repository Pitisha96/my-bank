package com.pitisha.project.mybank.transactionservice.infrastructure.client.exception;

public class AccountServiceTechnicalException extends AccountServiceException {

    private static final String TECHNICAL_EXCEPTION_MESSAGE = "Technical exception";

    public AccountServiceTechnicalException() {
        super(TECHNICAL_EXCEPTION_MESSAGE);
    }

    public AccountServiceTechnicalException(final String message) {
        super(message);
    }
}
