package com.pitisha.project.mybank.transactionservice.api.dto.request;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "one account operation", description = "One account operation")
public record OneAccountOperation(

        @Schema(description = "account identifier from/to which will debit/credit funds", example = "3f7cc511-8dd7-4588-b397-6518556adf57")
        @NotNull
        UUID accountId,

        @Schema(description = "transaction amount", example = "1234.56")
        @NotNull
        @Positive
        @Digits(integer = 18, fraction = 2)
        BigDecimal amount,

        @Schema(description = "transaction currency", example = "BYN")
        @NotNull
        AccountCurrency currency
) { }
