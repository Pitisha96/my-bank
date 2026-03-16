package com.pitisha.project.mybank.accountservice.api.dto.response;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(
       UUID id,
       String number,
       UUID ownerId,
       BigDecimal balance,
       AccountCurrency currency,
       AccountStatus status,
       OffsetDateTime createdAt,
       OffsetDateTime updatedAt
) { }
