package com.pitisha.project.mybank.accountservice.api.dto.response;

public enum ErrorCode {

    INVALID_REQUEST,
    VALIDATION_ERROR,
    RESOURCE_NOT_FOUND,
    ILLEGAL_STATUS_STATE,
    ILLEGAL_OPERATION_ORDER,
    ILLEGAL_BALANCE_STATE,
    CONCURRENT_MODIFICATION,
    INTERNAL_ERROR
}
