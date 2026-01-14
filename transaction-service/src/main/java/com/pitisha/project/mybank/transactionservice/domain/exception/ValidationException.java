package com.pitisha.project.mybank.transactionservice.domain.exception;

import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.VALIDATION_ERROR;

public class ValidationException extends ApplicationException{

    public ValidationException(String message) {
        super(VALIDATION_ERROR, message);
    }
}
