package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.accountservice.api.validation.ValidEnum;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountRequest(

        @NotNull
        @Positive
        @Digits(integer = 18, fraction = 2)
        BigDecimal amount,

        @NotNull
        @ValidEnum(enumClass = AccountCurrency.class)
        String currency
) {}
