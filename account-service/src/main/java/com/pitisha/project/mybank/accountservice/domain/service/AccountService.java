package com.pitisha.project.mybank.accountservice.domain.service;

import com.pitisha.project.mybank.accountservice.api.dto.request.AccountFilter;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountResponse;
import com.pitisha.project.mybank.accountservice.api.dto.response.AccountPageResponse;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.util.Optional;
import java.util.UUID;

public interface AccountService {

    AccountPageResponse findAll(AccountFilter filter);

    Optional<AccountResponse> findById(UUID id);

    AccountResponse create(UUID ownerId, AccountCurrency currency);

    AccountResponse updateStatus(UUID id, AccountStatus status);
}
