package com.pitisha.project.mybank.accountservice.api.dto.response;

import org.springframework.http.HttpStatus;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.FORBIDDEN;
import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.UNAUTHORIZED;
import static java.time.OffsetDateTime.now;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        ErrorCode errorCode,
        String message,
        Map<String, Object> details,
        OffsetDateTime timestamp
) {

    public static ErrorResponse of(final ErrorCode errorCode, final String message, final Map<String, Object> details) {
        return new ErrorResponse(errorCode, message, details, now());
    }

    public static ErrorResponse of(final ErrorCode errorCode, final String message) {
        return new ErrorResponse(errorCode, message, null, now());
    }

    public static ErrorResponse unauthorized() {
        return new ErrorResponse(UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), null, now());
    }

    public static ErrorResponse forbidden() {
        return new ErrorResponse(FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), null, now());
    }

    public static ErrorResponse internalServerError() {
        return new ErrorResponse(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null, now());
    }
}
