package com.pitisha.project.mybank.accountservice.domain.service;

import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.CANCEL;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.CREDIT;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.RESERVE;
import static com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType.WITHDRAW;
import static com.pitisha.project.mybank.accountservice.domain.util.ArgumentValidationUtils.requireNonNullOrElseThrow;
import static com.pitisha.project.mybank.accountservice.domain.util.ArgumentValidationUtils.requirePositiveAmount;
import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_CREDITED_TOPIC;
import static com.pitisha.project.mybank.kafka.topic.TopicName.ACCOUNT_WITHDRAWN_TOPIC;
import static java.util.Objects.isNull;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOperationType;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountOutboxEntity;
import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountOperationRepository;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountRepository;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalBalanceStateException;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalOperationOrderException;
import com.pitisha.project.mybank.accountservice.domain.exception.IllegalStatusStateException;
import com.pitisha.project.mybank.accountservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.accountservice.domain.repository.AccountsOutboxRepository;
import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import com.pitisha.project.mybank.kafka.event.AccountCreditedEvent;
import com.pitisha.project.mybank.kafka.event.AccountWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOperationServiceImpl implements AccountOperationService {

    private final AccountOperationRepository accountOperationRepository;
    private final AccountRepository accountRepository;
    private final AccountsOutboxRepository accountsOutboxRepository;
    private final ExchangeService exchangeService;
    private final JsonMapper jsonMapper = new JsonMapper();

    private static final String ACCOUNT_ID_MUST_NOT_BE_NULL = "Account id must not be null";
    private static final String TRANSACTION_ID_MUST_NOT_BE_NULL = "Transaction id must not be null";
    private static final String AMOUNT_MUST_NOT_BE_NULL = "Amount must not be null";
    private static final String CURRENCY_MUST_NOT_BE_NULL = "Currency must not be null";
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
    private static final String ILLEGAL_ACCESS = "Illegal access";

    @Transactional
    @Override
    public void reserve(final UUID initiator, final UUID txId, final UUID accountId, final BigDecimal amount, final AccountCurrency currency) {
        validateParams(txId, accountId, amount, currency);
        if (accountOperationRepository.insertIfNotExists(txId, RESERVE.name(), accountId, amount, currency.name()) == 0) {
            log.info(ALREADY_PROCESSED, RESERVE.name(), txId, accountId);
            return;
        }
        final var account = lockAccountForUpdate(accountId);
        if (isNull(initiator) || !account.getOwnerId().equals(initiator)) {
            throw new AccessDeniedException(ILLEGAL_ACCESS);
        }
        if (!account.canReserve()) {
            logAndThrowIllegalStatusState(RESERVE, txId, accountId, account.getStatus());
        }
        final BigDecimal exchanged = exchangeService.exchange(amount, currency, account.getCurrency());
        if (!account.canReserve(exchanged)) {
            logAndThrowIllegalBalanceState(RESERVE, txId, accountId, INSUFFICIENT_FUNDS);
        }
        account.setReserved(account.getReserved().add(exchanged));
        accountRepository.save(account);
        logCompletion(RESERVE, txId, accountId);
    }

    @Transactional
    @Override
    public void withdraw(final UUID txId) {
        final var op = lockOperationForUpdate(txId);
        if (accountOperationRepository.insertIfNotExists(txId, WITHDRAW.name(), op.getAccountId(), op.getAmount(), op.getCurrency().name()) == 0) {
            log.info(ALREADY_TERMINATED, WITHDRAW.name(), txId);
            return;
        }
        final var account = lockAccountForUpdate(op.getAccountId());
        final BigDecimal exchanged = exchangeService.exchange(op.getAmount(), op.getCurrency(), account.getCurrency());
        if (!account.canWithdraw()) {
            logAndThrowIllegalStatusState(WITHDRAW, txId, op.getAccountId(), account.getStatus());
        }
        if (account.getReserved().compareTo(exchanged) < 0) {
            logAndThrowIllegalBalanceState(WITHDRAW, txId, op.getAccountId(), RESERVED_BALANCE_CORRUPTED);
        }
        account.setReserved(account.getReserved().subtract(exchanged));
        account.setBalance(account.getBalance().subtract(exchanged));
        final var outbox = new AccountOutboxEntity();
        outbox.setTopic(ACCOUNT_WITHDRAWN_TOPIC.getTopicName());
        outbox.setPayload(jsonMapper.writeValueAsString(
                new AccountWithdrawnEvent(op.getAccountId(), account.getOwnerId(), op.getCurrency(), op.getAmount())
        ));
        accountRepository.save(account);
        accountsOutboxRepository.save(outbox);
        logCompletion(WITHDRAW, txId, op.getAccountId());
    }

    @Transactional
    @Override
    public void cancel(final UUID txId) {
        final var op = lockOperationForUpdate(txId);
        if (accountOperationRepository.insertIfNotExists(txId, CANCEL.name(), op.getAccountId(), op.getAmount(), op.getCurrency().name()) == 0) {
            log.info(ALREADY_TERMINATED, CANCEL.name(), txId);
            return;
        }
        final var account = lockAccountForUpdate(op.getAccountId());
        final BigDecimal exchanged = exchangeService.exchange(op.getAmount(), op.getCurrency(), account.getCurrency());
        if (account.getReserved().compareTo(exchanged) < 0) {
            logAndThrowIllegalBalanceState(CANCEL, txId, op.getAccountId(), RESERVED_BALANCE_CORRUPTED);
        }
        if (!account.canCancel()) {
            logAndThrowIllegalStatusState(CANCEL, txId, op.getAccountId(), account.getStatus());
        }
        account.setReserved(account.getReserved().subtract(exchanged));
        accountRepository.save(account);
        logCompletion(CANCEL, txId, op.getAccountId());
    }

    @Transactional
    @Override
    public void credit(final UUID initiator, final UUID txId, final UUID accountId, final BigDecimal amount, final AccountCurrency currency) {
        validateParams(txId, accountId, amount, currency);
        if (accountOperationRepository.insertIfNotExists(txId, CREDIT.name(), accountId, amount, currency.name()) == 0) {
            log.info(ALREADY_PROCESSED, CREDIT.name(), txId, accountId);
            return;
        }
        final var account = lockAccountForUpdate(accountId);
        if (isNull(initiator) || !account.getOwnerId().equals(initiator)) {
            throw new AccessDeniedException(ILLEGAL_ACCESS);
        }
        if (!account.canCredit()) {
            logAndThrowIllegalStatusState(CREDIT, txId, accountId, account.getStatus());
        }
        final BigDecimal exchanged = exchangeService.exchange(amount, currency, account.getCurrency());
        account.setBalance(account.getBalance().add(exchanged));
        final var outbox = new AccountOutboxEntity();
        outbox.setTopic(ACCOUNT_CREDITED_TOPIC.getTopicName());
        outbox.setPayload(jsonMapper.writeValueAsString(
                new AccountCreditedEvent(accountId, account.getOwnerId(), currency, amount)
        ));
        accountRepository.save(account);
        accountsOutboxRepository.save(outbox);
    }

    private void validateParams(final UUID transactionId,
                                final UUID number,
                                final BigDecimal amount,
                                final AccountCurrency currency) {
        requireNonNullOrElseThrow(transactionId, TRANSACTION_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(number, ACCOUNT_ID_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(amount, AMOUNT_MUST_NOT_BE_NULL);
        requireNonNullOrElseThrow(currency, CURRENCY_MUST_NOT_BE_NULL);
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
