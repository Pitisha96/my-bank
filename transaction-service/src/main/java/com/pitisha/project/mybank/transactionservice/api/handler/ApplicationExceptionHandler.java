package com.pitisha.project.mybank.transactionservice.api.handler;

import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.INVALID_PARAMETER;
import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.INVALID_REQUEST_BODY;
import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.MISSING_PARAMETER;
import static com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode.VALIDATION_ERROR;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.status;

import com.fasterxml.jackson.core.JsonParseException;
import com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorCode;
import com.pitisha.project.mybank.transactionservice.api.dto.response.ErrorResponse;
import com.pitisha.project.mybank.transactionservice.domain.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import javax.naming.AuthenticationException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final String VALIDATION_ERROR_MESSAGE = "Validation error";
    private static final String TYPE_MISMATCH_ERROR_MESSAGE = "Parameter '%s' must be of type %s";
    private static final String MISSING_PARAMETER_MESSAGE = "Required parameter '%s' : '%s' is missing";
    private static final String UNKNOWN = "unknown";
    private static final String MALFORMED_JSON = "Malformed JSON";
    private static final String INVALID_REQUEST_BODY_MESSAGE = "Invalid request body";
    private static final String COMMA = ", ";
    private static final String INVALID_VALUE_FOR_FIELD = "Invalid value for field '%s'";
    private static final String INVALID_REQUEST_STRUCTURE = "Invalid request structure";
    private static final String INVALID_VALUE = "Invalid value '";
    private static final String FOR_FIELD = "' for field ";
    private static final String ALLOWED_VALUES = ". Allowed values: [";
    private static final String END_SQUARE_BRACKET = "]";

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
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return badRequest()
                .body(ErrorResponse.of(
                        VALIDATION_ERROR,
                        VALIDATION_ERROR_MESSAGE,
                        getValidationDetailedMessages(e)
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        return badRequest()
                .body(ErrorResponse.of(
                        INVALID_PARAMETER,
                        TYPE_MISMATCH_ERROR_MESSAGE.formatted(
                                e.getName(),
                                isNull(e.getRequiredType()) ? UNKNOWN : e.getRequiredType().getSimpleName()
                        )
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParam(final MissingServletRequestParameterException e) {
        return badRequest()
                .body(ErrorResponse.of(
                        MISSING_PARAMETER,
                        MISSING_PARAMETER_MESSAGE.formatted(e.getParameterName(), e.getParameterType())
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(final HttpMessageNotReadableException e) {
        return badRequest()
                .body(ErrorResponse.of(
                        INVALID_REQUEST_BODY,
                        resolveMessageNotReadableMessage(e)
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(final Exception e) throws Exception {
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e;
        }
        return internalServerError()
                .body(ErrorResponse.internal());
    }

    private Map<String, Object> getValidationDetailedMessages(final MethodArgumentNotValidException e) {
        return e.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap((error) ->
                        ((FieldError) error).getField(), error -> defaultString(error.getDefaultMessage()))
                );
    }

    private HttpStatus mapHttpStatus(final ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;

            case FORBIDDEN -> HttpStatus.FORBIDDEN;

            case INVALID_PARAMETER,
                 MISSING_PARAMETER,
                 INVALID_REQUEST_BODY,
                 VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;

            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String resolveMessageNotReadableMessage(final HttpMessageNotReadableException e) {
        final Throwable cause = getRootCause(e);
        if (cause instanceof InvalidFormatException ife) {
            return handleInvalidFormat(ife);
        }
        if (cause instanceof MismatchedInputException mie) {
            return handleMismatchedInputException(mie);
        }
        if (cause instanceof JsonParseException jpe) {
            return MALFORMED_JSON;
        }
        return INVALID_REQUEST_BODY_MESSAGE;
    }

    private Throwable getRootCause(final Throwable e) {
        Throwable result = e;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }

    private String handleInvalidFormat(final InvalidFormatException e) {
        final String field = e.getPath().getFirst().getPropertyName();
        final StringBuilder sb = new StringBuilder();
        sb
                .append(INVALID_VALUE)
                .append(e.getValue())
                .append(FOR_FIELD)
                .append(field);

        if (e.getTargetType().isEnum()) {
            final String allowed = stream(e.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(joining(COMMA));
            sb
                    .append(ALLOWED_VALUES)
                    .append(allowed)
                    .append(END_SQUARE_BRACKET);
        }
        return sb.toString();
    }

    private String handleMismatchedInputException(final MismatchedInputException e) {
        final String field = e.getPath().getFirst().getPropertyName();
        return nonNull(field)
                ? INVALID_VALUE_FOR_FIELD.formatted(field)
                : INVALID_REQUEST_STRUCTURE;
    }
}
