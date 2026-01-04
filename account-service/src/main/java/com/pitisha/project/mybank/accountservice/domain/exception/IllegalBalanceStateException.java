package com.pitisha.project.mybank.accountservice.domain.exception;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.ILLEGAL_BALANCE_STATE;

public class IllegalBalanceStateException extends ApplicationException {

    public IllegalBalanceStateException(final String message) {
        super(ILLEGAL_BALANCE_STATE, message);
    }
}
