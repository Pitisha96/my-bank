package com.pitisha.project.mybank.kafka.event;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountCreditedEvent (
    UUID accountId,
    UUID ownerId,
    AccountCurrency currency,
    BigDecimal amount
) implements AccountKafkaEvent { }
