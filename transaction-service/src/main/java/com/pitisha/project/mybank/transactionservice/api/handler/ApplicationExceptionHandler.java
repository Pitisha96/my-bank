package com.pitisha.project.mybank.transactionservice.api.handler;

import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.VALIDATION_ERROR;
import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorResponse.of;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.status;

import com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode;
import com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorResponse;
import com.pitisha.project.mybank.transactionservice.domain.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final String VALIDATION_ERROR_MESSAGE = "validation error";

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(final ApplicationException e) {
        return status(mapHttpStatus(e.getErrorCode()))
                .body(
                        of(
                                e.getErrorCode(),
                                e.getMessage(),
                                e.getDetails()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return status(BAD_REQUEST)
                .body(
                        of(
                                VALIDATION_ERROR,
                                VALIDATION_ERROR_MESSAGE,
                                getValidationDetailedMessages(e)
                        )
                );
    }

    private Map<String, Object> getValidationDetailedMessages(final MethodArgumentNotValidException e) {
        return e.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap((error) ->
                        ((FieldError) error).getField(), error -> defaultString(error.getDefaultMessage()))
                );
    }

    private HttpStatus mapHttpStatus(final ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_ERROR -> BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> NOT_FOUND;
        };
    }
}
