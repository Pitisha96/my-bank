package com.pitisha.project.mybank.accountservice.domain.service;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.CANCEL;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.CREDIT;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.RESERVE;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.WITHDRAW;
import static com.pitisha.project.mybank.accountservice.domain.util.ArgumentValidationUtils.requireNonNullOrElseThrow;
import static com.pitisha.project.mybank.accountservice.domain.util.ArgumentValidationUtils.requirePositiveAmount;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountOperationRepository;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountRepository;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalBalanceStateException;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalOperationOrderException;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalStatusStateException;
import com.pitisha.project.mybank.accountservice.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOperationServiceImpl implements AccountOperationService {

    private final AccountOperationRepository accountOperationRepository;
    private final AccountRepository accountRepository;

    private static final String ACCOUNT_ID_MUST_NOT_BE_NULL = "Account id must not be null";
    private static final String TRANSACTION_ID_MUST_NOT_BE_NULL = "Transaction id must not be null";
    private static final String AMOUNT_MUST_NOT_BE_NULL = "Amount must not be null";
    private static final String AMOUNT_MUST_BE_POSITIVE = "Amount must be positive";
    private static final String ACCOUNT_IS_NOT_DEFINED = "Account is not defined!";
    private static final String ACCOUNT_NOT_AVAILABLE = "Account is %s!";
    private static final String INSUFFICIENT_FUNDS = "Insufficient funds";
    private static final String RESERVED_BALANCE_CORRUPTED = "Reserved balance corrupted";
    private static final String MUST_BE_RESERVED_OPERATION = "Must be reserved operation";
    private static final String ALREADY_PROCESSED = "{} operation for tx={}, account={} is already processed";
    private static final String ALREADY_TERMINATED = "Skip {}, because tx={} is already terminated";
    private static final String FAILED_STATUS_REASON = "{} operation for tx={}, account={} is failed. Status: {}";
    private static final String FAILED_BALANCE_REASON = "{} operation for tx={}, account={} is failed. Reason: {}";
    private static final String COMPLETED = "{} operation for tx={}, account={} is completed";

    @Transactional
    @Override
    public void reserve(final UUID txId, final UUID accountId, final BigDecimal amount) {
        validateParams(txId, accountId, amount);
        if (accountOperationRepository.insertIfNotExists(txId, RESERVE.name(), accountId, amount) == 0) {
            log.info(ALREADY_PROCESSED, RESERVE.name(), txId, accountId);
            return;
        }
        final var account = lockAccountForUpdate(accountId);
        if (!account.canReserve()) {
            logAndThrowIllegalStatusState(RESERVE, txId, accountId, account.getStatus());
        }
        if (!account.canReserve(amount)) {
            logAndThrowIllegalBalanceState(RESERVE, txId, accountId, INSUFFICIENT_FUNDS);
        }
        account.setReserved(account.getReserved().add(amount));
        accountRepository.save(account);
        logCompletion(RESERVE, txId, accountId);
    }

    @Transactional
    @Override
    public void withdraw(final UUID txId) {
        final var op = lockOperationForUpdate(txId);
        if (accountOperationRepository.insertIfNotExists(txId, WITHDRAW.name(), op.getAccountId(), op.getAmount()) == 0) {
            log.info(ALREADY_TERMINATED, WITHDRAW.name(), txId);
            return;
        }
        final var account = lockAccountForUpdate(op.getAccountId());
        if (!account.canWithdraw()) {
            logAndThrowIllegalStatusState(WITHDRAW, txId, op.getAccountId(), account.getStatus());
        }
        if (account.getReserved().compareTo(op.getAmount()) < 0) {
            logAndThrowIllegalBalanceState(WITHDRAW, txId, op.getAccountId(), RESERVED_BALANCE_CORRUPTED);
        }
        account.setReserved(account.getReserved().subtract(op.getAmount()));
        account.setBalance(account.getBalance().subtract(op.getAmount()));
        accountRepository.save(account);
        logCompletion(WITHDRAW, txId, op.getAccountId());
    }

    @Transactional
    @Override
    public void cancel(final UUID txId) {
        final var op = lockOperationForUpdate(txId);
        if (accountOperationRepository.insertIfNotExists(txId, CANCEL.name(), op.getAccountId(), op.getAmount()) == 0) {
            log.info(ALREADY_TERMINATED, CANCEL.name(), txId);
            return;
        }
        final var account = lockAccountForUpdate(op.getAccountId());
        if (account.getReserved().compareTo(op.getAmount()) < 0) {
            logAndThrowIllegalBalanceState(CANCEL, txId, op.getAccountId(), RESERVED_BALANCE_CORRUPTED);
        }
        if (!account.canCancel()) {
            logAndThrowIllegalStatusState(CANCEL, txId, op.getAccountId(), account.getStatus());
        }
        account.setReserved(account.getReserved().subtract(op.getAmount()));
        accountRepository.save(account);
        logCompletion(CANCEL, txId, op.getAccountId());
    }

    @Transactional
    @Override
    public void credit(final UUID txId, final UUID accountId, final BigDecimal amount) {
        validateParams(txId, accountId, amount);
        if (accountOperationRepository.insertIfNotExists(txId, CREDIT.name(), accountId, amount) == 0) {
            log.info(ALREADY_PROCESSED, CREDIT.name(), txId, accountId);
            return;
        }
        final var account = lockAccountForUpdate(accountId);
        if (!account.canCredit()) {
            logAndThrowIllegalStatusState(CREDIT, txId, accountId, account.getStatus());
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private void validateParams(final UUID transactionId, final UUID number, final BigDecimal amount) {
        requireNonNullOrElseThrow(transactionId, TRANSACTION_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(number, ACCOUNT_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(amount, AMOUNT_MUST_NOT_BE_NULL);
        requirePositiveAmount(amount, AMOUNT_MUST_BE_POSITIVE);
    }

    private AccountOperationEntity lockOperationForUpdate(final UUID transactionId) {
        requireNonNullOrElseThrow(transactionId, TRANSACTION_ID_MUST_NOT_BE_NULL);
        return accountOperationRepository.findByTxIdAndTypeForUpdate(transactionId, RESERVE)
                .orElseThrow(() -> new IllegalOperationOrderException(MUST_BE_RESERVED_OPERATION));
    }

    private AccountEntity lockAccountForUpdate(final UUID accountId) {
        return accountRepository.findByNumberForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_IS_NOT_DEFINED));
    }

    private void logAndThrowIllegalStatusState(final AccountOperationType op,
                                               final UUID txId,
                                               final UUID accountId,
                                               final AccountStatus status) {
        log.warn(FAILED_STATUS_REASON, op.name(), txId, accountId, status.name());
        throw new IllegalStatusStateException(ACCOUNT_NOT_AVAILABLE.formatted(status.name()));
    }

    private void logAndThrowIllegalBalanceState(final AccountOperationType op,
                                                final UUID txId,
                                                final UUID accountId,
                                                final String reason) {
        log.warn(FAILED_BALANCE_REASON, op.name(), txId, accountId, reason);
        throw new IllegalBalanceStateException(reason);
    }

    private void logCompletion(final AccountOperationType op,
                               final UUID transactionId,
                               final UUID accountId) {
        log.info(COMPLETED, op.name(), transactionId, accountId);
    }
}
