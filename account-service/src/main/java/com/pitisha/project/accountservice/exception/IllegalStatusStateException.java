package com.pitisha.project.accountservice.exception;

public class IllegalStatusStateException extends RuntimeException {

    public IllegalStatusStateException(final String message) {
        super(message);
    }
}
