package com.pitisha.project.accountservice.exception;

public class IllegalBalanceStateException extends RuntimeException {

    public IllegalBalanceStateException(final String message) {
        super(message);
    }
}
