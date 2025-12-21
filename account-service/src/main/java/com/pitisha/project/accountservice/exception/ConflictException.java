package com.pitisha.project.accountservice.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(final String message) {
        super(message);
    }
}
