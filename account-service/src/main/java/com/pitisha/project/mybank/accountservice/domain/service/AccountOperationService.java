package com.pitisha.project.mybank.accountservice.domain.service;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountOperationService {
    void reserve(UUID txId, UUID accountId, BigDecimal amount, AccountCurrency currency);
    void withdraw(UUID txId);
    void cancel(UUID txId);
    void credit(UUID txId, UUID accountId, BigDecimal amount, AccountCurrency currency);
}
