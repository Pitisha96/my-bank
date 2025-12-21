package com.pitisha.project.accountservice.api.dto.request;

import com.pitisha.project.accountservice.api.validation.ValidEnum;
import com.pitisha.project.accountservice.domain.entity.AccountCurrency;
import com.pitisha.project.accountservice.domain.entity.AccountStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountFilter(

        UUID ownerId,

        @ValidEnum(enumClass = AccountCurrency.class)
        String currency,

        BigDecimal balanceFrom,

        BigDecimal balanceTo,

        @ValidEnum(enumClass = AccountStatus.class)
        String status,

        @PastOrPresent
        LocalDate createdFrom,

        @PastOrPresent
        LocalDate createdTo,

        @NotNull
        @PositiveOrZero
        Integer page,

        @NotNull
        @Positive
        @Max(50)
        Integer pageSize
) {}
