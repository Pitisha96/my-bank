package com.pitisha.project.mybank.transactionservice.api.dto.response;

import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.FORBIDDEN;
import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.UNAUTHORIZED;
import static java.time.LocalDateTime.now;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(name = "error response", description = "Provided error response")
public record ErrorResponse(

        @Schema(description = "Provided error code", example = "VALIDATION_ERROR")
        ErrorCode errorCode,

        @Schema(description = "Provided error message", example = "Validation error")
        String message,

        @Schema(description = "Provides detailed message")
        Map<String, Object> details,

        @Schema(description = "Provides time of error")
        LocalDateTime timestamp
) {

    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Something went wrong";

    public static ErrorResponse of(final ErrorCode errorCode, final String message, final Map<String, Object> details) {
        return new ErrorResponse(errorCode, message, details, now());
    }

    public static ErrorResponse of(final ErrorCode errorCode, final String message) {
        return new ErrorResponse(errorCode, message, null, now());
    }

    public static ErrorResponse unauthorized() {
        return new ErrorResponse(UNAUTHORIZED, UNAUTHORIZED_MESSAGE, null, now());
    }

    public static ErrorResponse forbidden() {
        return new ErrorResponse(FORBIDDEN, FORBIDDEN_MESSAGE, null, now());
    }

    public static ErrorResponse internal() {
        return new ErrorResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE, null, now());
    }
}
