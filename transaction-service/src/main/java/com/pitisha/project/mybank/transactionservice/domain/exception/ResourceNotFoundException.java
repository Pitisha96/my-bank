package com.pitisha.project.mybank.transactionservice.domain.exception;

import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.RESOURCE_NOT_FOUND;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(final String message) {
        super(RESOURCE_NOT_FOUND, message);
    }
}
