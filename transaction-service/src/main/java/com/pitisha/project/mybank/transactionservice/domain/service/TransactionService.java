package com.pitisha.project.mybank.transactionservice.domain.service;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import com.pitisha.project.mybank.transactionservice.api.dto.response.TransactionResponse;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface TransactionService {

    Optional<TransactionResponse> findById(UUID txId);

    UUID startDeposit(UUID initiator, UUID accountId, BigDecimal amount, AccountCurrency currency);

    UUID startWithdraw(UUID initiator, UUID accountId, BigDecimal amount, AccountCurrency currency);

    UUID startTransfer(UUID initiator, UUID fromAccountId, UUID toAccountId, BigDecimal amount, AccountCurrency currency);
}
