package com.pitisha.project.accountservice.domain.service;

import com.pitisha.project.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.accountservice.domain.entity.AccountCurrency;
import com.pitisha.project.accountservice.domain.entity.AccountStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    AccountPageResponse findAll(AccountFilter filter);
    Optional<AccountResponse> findByNumber(UUID number);
    List<AccountResponse> findByOwnerId(UUID ownerId);
    AccountResponse create(UUID userId, AccountCurrency currency);
    AccountResponse updateStatus(UUID number, AccountStatus status);
    AccountResponse deposit(UUID number, BigDecimal amount);
    AccountResponse withdraw(UUID number, BigDecimal amount);
}
