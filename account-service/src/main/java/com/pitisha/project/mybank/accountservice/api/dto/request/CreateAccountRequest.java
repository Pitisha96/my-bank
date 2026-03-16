package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(

        UUID ownerId,

        @NotNull
        AccountCurrency currency
) {
}
