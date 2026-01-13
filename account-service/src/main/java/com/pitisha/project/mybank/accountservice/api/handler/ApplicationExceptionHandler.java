package com.pitisha.project.mybank.accountservice.api.handler;

import static com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode.VALIDATION_ERROR;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.ResponseEntity.status;

import com.pitisha.project.mybank.accountservice.api.dto.response.ErrorCode;
import com.pitisha.project.mybank.accountservice.api.dto.response.ErrorResponse;
import com.pitisha.project.mybank.accountservice.domain.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    private static final String VALIDATION_ERROR_MESSAGE = "validation error";

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(final ApplicationException e) {
        return status(mapHttpStatus(e.getErrorCode()))
                .body(ErrorResponse.of(
                        e.getErrorCode(),
                        e.getMessage(),
                        e.getDetails()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException e) {
        return status(BAD_REQUEST)
                .body(ErrorResponse.of(
                        VALIDATION_ERROR,
                        VALIDATION_ERROR_MESSAGE,
                        getValidationDetailedMessages(e)
                ));
    }

    private Map<String, Object> getValidationDetailedMessages(final MethodArgumentNotValidException e) {
        return e.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap((error) ->
                        ((FieldError) error).getField(), error -> defaultString(error.getDefaultMessage()))
                );
    }

    private HttpStatus mapHttpStatus(final ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_ERROR,
                 INVALID_REQUEST -> BAD_REQUEST;

            case RESOURCE_NOT_FOUND -> NOT_FOUND;

            case ILLEGAL_STATUS_STATE,
                 ILLEGAL_BALANCE_STATE,
                 ILLEGAL_OPERATION_ORDER -> UNPROCESSABLE_ENTITY;

            case CONCURRENT_MODIFICATION -> CONFLICT;

            default -> INTERNAL_SERVER_ERROR;
        };
    }
}
