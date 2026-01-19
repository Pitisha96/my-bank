package com.pitisha.project.mybank.transactionservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "error code", description = "Provides error code for error response")
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
