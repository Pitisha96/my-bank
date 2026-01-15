package com.pitisha.project.mybank.accountservice.domain.util;

import static java.util.Objects.isNull;

import com.pitisha.project.mybank.accountservice.domain.exception.ValidationException;

import java.math.BigDecimal;

public class ArgumentValidationUtils {

    public static void requireNonNullOrElseThrow(final Object object, final String message) {
        if (isNull(object)) {
            throw new ValidationException(message);
        }
    }

    public static void requirePositiveAmount(final BigDecimal amount, final String message) {
        if (amount.signum() > 0) {
            return;
        }
        throw new ValidationException(message);
    }
}
