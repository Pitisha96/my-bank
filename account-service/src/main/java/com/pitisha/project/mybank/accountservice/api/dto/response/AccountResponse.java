package com.pitisha.project.mybank.accountservice.api.dto.response;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
       UUID number,
       UUID ownerId,
       AccountCurrency currency,
       BigDecimal balance,
       AccountStatus status,
       LocalDateTime createdAt
) { }
