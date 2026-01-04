package com.pitisha.project.mybank.accountservice.domain.exception;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.ILLEGAL_STATUS_STATE;

public class IllegalStatusStateException extends ApplicationException {

    public IllegalStatusStateException(final String message) {
        super(ILLEGAL_STATUS_STATE, message);
    }
}
