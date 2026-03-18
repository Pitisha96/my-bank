package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountFilter(

        UUID ownerId,

        AccountCurrency currency,

        @PositiveOrZero
        @Digits(integer = 19, fraction = 2)
        BigDecimal balanceFrom,

        @PositiveOrZero
        @Digits(integer = 19, fraction = 2)
        BigDecimal balanceTo,

        AccountStatus status,

        @PastOrPresent
        OffsetDateTime createdFrom,

        @PastOrPresent
        OffsetDateTime createdTo,

        @PastOrPresent
        OffsetDateTime updatedFrom,

        @PastOrPresent
        OffsetDateTime updatedTo,

        @NotNull
        @PositiveOrZero
        Integer page,

        @NotNull
        @Positive
        @Max(50)
        Integer pageSize
) { }
