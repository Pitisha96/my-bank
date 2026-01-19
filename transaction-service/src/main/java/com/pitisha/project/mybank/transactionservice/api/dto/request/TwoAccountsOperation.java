package com.pitisha.project.mybank.transactionservice.api.dto.request;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "two accounts operation", description = "Two accounts operation")
public record TwoAccountsOperation(

        @Schema(description = "account identifier from which will debit funds", example = "3f7cc511-8dd7-4588-b397-6518556adf57")
        @NotNull
        UUID fromAccountId,

        @Schema(description = "account identifier to which will credit funds",  example = "006238d0-e297-45b9-a54c-cd5a966c4d04")
        @NotNull
        UUID toAccountId,

        @Schema(description = "transaction amount", example = "1234.56")
        @NotNull
        @Positive
        @Digits(integer = 18, fraction = 2)
        BigDecimal amount,

        @Schema(description = "transaction currency", example = "BYN")
        @NotNull
        AccountCurrency currency
) { }
