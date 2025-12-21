package com.pitisha.project.accountservice.api.dto.response;

import com.pitisha.project.accountservice.domain.entity.AccountCurrency;
import com.pitisha.project.accountservice.domain.entity.AccountStatus;

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
