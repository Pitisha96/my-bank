package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.accountservice.api.validation.ValidEnum;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(

        @NotNull
        UUID ownerId,

        @NotNull
        @ValidEnum(enumClass = AccountCurrency.class)
        String currency
) {
}
