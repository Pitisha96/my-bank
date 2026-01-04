package com.pitisha.project.mybank.accountservice.domain.exception;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.ILLEGAL_OPERATION_ORDER;

public class IllegalOperationOrderException extends ApplicationException {

    public IllegalOperationOrderException(final String message) {
        super(ILLEGAL_OPERATION_ORDER, message);
    }
}
