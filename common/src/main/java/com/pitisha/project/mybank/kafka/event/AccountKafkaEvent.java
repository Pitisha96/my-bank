package com.pitisha.project.mybank.kafka.event;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.util.UUID;

public interface AccountKafkaEvent {
    UUID accountId();
    UUID ownerId();
    AccountCurrency currency();
}
