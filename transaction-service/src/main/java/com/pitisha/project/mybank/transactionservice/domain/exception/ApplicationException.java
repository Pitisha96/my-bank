package com.pitisha.project.mybank.transactionservice.domain.exception;

import com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public ApplicationException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }
}
