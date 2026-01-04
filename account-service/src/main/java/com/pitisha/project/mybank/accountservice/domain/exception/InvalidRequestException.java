package com.pitisha.project.mybank.accountservice.domain.exception;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.INVALID_REQUEST;

public class InvalidRequestException extends ApplicationException {

    public InvalidRequestException(final String message) {
        super(INVALID_REQUEST, message);
    }
}
