package com.pitisha.project.mybank.accountservice.domain.service;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {

    AccountPageResponse findAll(AccountFilter filter);

    Optional<AccountResponse> findByNumber(UUID number);

    List<AccountResponse> findByOwnerId(UUID ownerId);

    AccountResponse create(UUID userId, AccountCurrency currency);

    AccountResponse updateStatus(UUID number, AccountStatus status);
}
