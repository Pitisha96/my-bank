package com.pitisha.project.mybank.transactionservice.domain.service;

import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.NEW;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionType.DEPOSIT;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionType.TRANSFER;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionType.WITHDRAW;
import static com.pitisha.project.mybank.transactionservice.domain.util.ArgumentValidationUtils.requireNonNullOrElseThrow;
import static com.pitisha.project.mybank.transactionservice.domain.util.ArgumentValidationUtils.requirePositiveAmount;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import com.pitisha.project.mybank.transactionservice.api.dto.response.TransactionResponse;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionEntity;
import com.pitisha.project.mybank.transactionservice.domain.event.TransactionCreatedEvent;
import com.pitisha.project.mybank.transactionservice.domain.mapper.TransactionMapper;
import com.pitisha.project.mybank.transactionservice.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private static final String ACCOUNT_ID_MUST_NOT_BE_NULL = "accountId must not be null";
    private static final String FROM_ACCOUNT_ID_MUST_NOT_BE_NULL = "fromAccountId must not be null";
    private static final String TO_ACCOUNT_ID_MUST_NOT_BE_NULL = "toAccountId must not be null";
    private static final String AMOUNT_MUST_NOT_BE_NULL = "amount must not be null";
    private static final String AMOUNT_MUST_BE_POSITIVE = "amount must be positive";
    private static final String TRANSACTION_ACCEPTED = "{} tx={} accepted";

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public Optional<TransactionResponse> findById(final UUID txId) {
        return transactionRepository.findById(txId)
                .map(transactionMapper::toDto);
    }

    @Transactional
    @Override
    public UUID startDeposit(final UUID initiator, final UUID accountId, final BigDecimal amount, final AccountCurrency currency) {
        validate(accountId, amount);
        final var tx = new TransactionEntity(initiator, DEPOSIT, NEW, null, accountId, amount, currency);
        transactionRepository.saveAndFlush(tx);
        log.info(TRANSACTION_ACCEPTED, DEPOSIT.name(), tx.getId());
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, tx.getId()));
        return tx.getId();
    }

    @Transactional
    @Override
    public UUID startWithdraw(final UUID initiator, final UUID accountId, final BigDecimal amount, final AccountCurrency currency) {
        validate(accountId, amount);
        final var tx = new TransactionEntity(initiator, WITHDRAW, NEW, accountId, null, amount, currency);
        transactionRepository.saveAndFlush(tx);
        log.info(TRANSACTION_ACCEPTED, WITHDRAW.name(), tx.getId());
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, tx.getId()));
        return tx.getId();
    }

    @Transactional
    @Override
    public UUID startTransfer(final UUID initiator, final UUID fromAccountId, final UUID toAccountId, final BigDecimal amount, final AccountCurrency currency) {
        validate(fromAccountId, toAccountId, amount);
        final var tx = new TransactionEntity(initiator, TRANSFER, NEW,  fromAccountId, toAccountId, amount, currency);
        transactionRepository.saveAndFlush(tx);
        log.info(TRANSACTION_ACCEPTED, TRANSFER.name(), tx.getId());
        eventPublisher.publishEvent(new TransactionCreatedEvent(this, tx.getId()));
        return tx.getId();
    }

    private void validate(final UUID accountId, final BigDecimal amount) {
        requireNonNullOrElseThrow(accountId, ACCOUNT_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(amount, AMOUNT_MUST_NOT_BE_NULL);
        requirePositiveAmount(amount, AMOUNT_MUST_BE_POSITIVE);
    }

    private void validate(final UUID fromAccountId, final UUID toAccountId, final BigDecimal amount) {
        requireNonNullOrElseThrow(fromAccountId, FROM_ACCOUNT_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(toAccountId, TO_ACCOUNT_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(amount, AMOUNT_MUST_NOT_BE_NULL);
        requirePositiveAmount(amount, AMOUNT_MUST_BE_POSITIVE);
    }
}
