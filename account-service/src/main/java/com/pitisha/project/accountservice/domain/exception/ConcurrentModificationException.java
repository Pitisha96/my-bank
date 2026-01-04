package com.pitisha.project.accountservice.domain.exception;

import static com.pitisha.project.accountservice.api.dto.response.ErrorCode.CONCURRENT_MODIFICATION;

public class ConcurrentModificationException extends ApplicationException {

    public ConcurrentModificationException(final String message) {
        super(CONCURRENT_MODIFICATION, message);
    }
}
