package com.pitisha.project.accountservice.api.validation;

import static java.lang.String.join;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private static final String VALUES = "{values}";
    private static final String COMMA = ", ";
    private Set<String> values;

    @Override
    public void initialize(final ValidEnum constraintAnnotation) {
        values = of(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(toSet());
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (isNull(value)) {
            return true;
        }
        if (!values.contains(value)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(
                            context.getDefaultConstraintMessageTemplate().replace(VALUES, join(COMMA, values))
                    )
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
