package com.pitisha.project.mybank.transactionservice.api.dto.request;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import com.pitisha.project.mybank.transactionservice.api.validation.ValidEnum;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TwoAccountsOperation(

        @NotNull
        UUID fromAccountId,

        @NotNull
        UUID toAccountId,

        @NotNull
        @Positive
        @Digits(integer = 18, fraction = 2)
        BigDecimal amount,

        @NotNull
        @ValidEnum(enumClass = AccountCurrency.class)
        String currency
) { }
