package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.accountservice.api.validation.ValidEnum;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountRequest(

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        @ValidEnum(enumClass = AccountCurrency.class)
        AccountCurrency currency
) {}
