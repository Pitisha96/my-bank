package com.pitisha.project.mybank.transactionservice.infrastructure.client.dto.request;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.math.BigDecimal;

public record AmountRequest(
        BigDecimal amount,
        AccountCurrency currency
) { }
