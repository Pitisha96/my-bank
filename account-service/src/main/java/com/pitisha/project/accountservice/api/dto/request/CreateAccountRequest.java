package com.pitisha.project.accountservice.api.dto.request;

import com.pitisha.project.accountservice.api.validation.ValidEnum;
import com.pitisha.project.accountservice.domain.entity.AccountCurrency;
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
