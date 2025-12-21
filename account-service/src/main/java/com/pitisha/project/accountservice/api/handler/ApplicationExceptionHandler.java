package com.pitisha.project.accountservice.api.handler;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.ResponseEntity.status;

import com.pitisha.project.accountservice.api.dto.response.ErrorResponse;
import com.pitisha.project.accountservice.api.dto.response.ValidationErrorResponse;
import com.pitisha.project.accountservice.exception.AccountFilterValidationException;
import com.pitisha.project.accountservice.exception.ConflictException;
import com.pitisha.project.accountservice.exception.IllegalBalanceStateException;
import com.pitisha.project.accountservice.exception.IllegalStatusStateException;
import com.pitisha.project.accountservice.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    private static final String VALIDATION_ERROR_MESSAGE = "%s : %s";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(final MethodArgumentNotValidException e) {
        return status(BAD_REQUEST).body(
                new ValidationErrorResponse(
                        now(),
                        BAD_REQUEST.value(),
                        BAD_REQUEST.getReasonPhrase(),
                        getValidationErrorMessages(e)
                )
        );
    }

    @ExceptionHandler(AccountFilterValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleAccountFilterValidationException(final AccountFilterValidationException e) {
        return status(BAD_REQUEST).body(
                new ValidationErrorResponse(
                        now(),
                        BAD_REQUEST.value(),
                        BAD_REQUEST.getReasonPhrase(),
                        singletonList(e.getMessage())
                )
        );
    }

    @ExceptionHandler({ResourceNotFoundException.class, IllegalStatusStateException.class, IllegalBalanceStateException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(final RuntimeException e) {
        return status(BAD_REQUEST).body(
                new ErrorResponse(
                        now(),
                        BAD_REQUEST.value(),
                        BAD_REQUEST.getReasonPhrase(),
                        e.getMessage()
                )
        );
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(final ConflictException e) {
        return status(CONFLICT).body(
                new ErrorResponse(
                        now(),
                        CONFLICT.value(),
                        CONFLICT.getReasonPhrase(),
                        e.getMessage()
                )
        );
    }

    private List<String> getValidationErrorMessages(final MethodArgumentNotValidException e) {
        return e.getBindingResult().getAllErrors().stream()
                .map(error -> VALIDATION_ERROR_MESSAGE.formatted(((FieldError)error).getField(), error.getDefaultMessage()))
                .toList();
    }
}
