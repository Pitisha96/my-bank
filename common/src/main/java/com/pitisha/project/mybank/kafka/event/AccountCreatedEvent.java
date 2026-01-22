package com.pitisha.project.mybank.kafka.event;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.util.UUID;

public record AccountCreatedEvent(
        UUID accountId,
        UUID ownerId,
        AccountCurrency currency
) implements AccountKafkaEvent { }
