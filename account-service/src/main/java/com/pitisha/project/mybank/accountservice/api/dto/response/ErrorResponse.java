package com.pitisha.project.mybank.accountservice.api.dto.response;

import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        ErrorCode errorCode,
        String message,
        Map<String, Object> details,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(final ErrorCode errorCode, final String message, final Map<String, Object> details) {
        return new ErrorResponse(errorCode, message, details, now());
    }
}
