package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.accountservice.api.validation.ValidEnum;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.validation.constraints.NotNull;

public record CreateSelfAccountRequest(

        @NotNull
        @ValidEnum(enumClass = AccountCurrency.class)
        String currency
) {
}
