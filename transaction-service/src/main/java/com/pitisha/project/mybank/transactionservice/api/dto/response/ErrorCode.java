package com.pitisha.project.mybank.transactionservice.api.dto.response;

public enum ErrorCode {
    UNAUTHORIZED,
    FORBIDDEN,
    INVALID_PARAMETER,
    MISSING_PARAMETER,
    INVALID_REQUEST_BODY,
    VALIDATION_ERROR,
    RESOURCE_NOT_FOUND,
    INTERNAL_SERVER_ERROR
}
