package com.pitisha.project.mybank.accountservice.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountRequest(

        @NotNull
        @Positive
        BigDecimal amount
) {}
