package com.pitisha.project.mybank.transactionservice.domain.saga;

import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.APPLIED;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.CANCELED;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.COMPLETED;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.FAILED;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.NEW;
import static com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus.RESERVED;
import static java.util.Objects.nonNull;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionEntity;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus;
import com.pitisha.project.mybank.transactionservice.domain.event.TransactionCreatedEvent;
import com.pitisha.project.mybank.transactionservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.transactionservice.domain.repository.TransactionRepository;
import com.pitisha.project.mybank.transactionservice.domain.service.TransactionStatusService;
import com.pitisha.project.mybank.transactionservice.infrastructure.client.AccountClient;
import com.pitisha.project.mybank.transactionservice.infrastructure.client.dto.request.AmountRequest;
import com.pitisha.project.mybank.transactionservice.infrastructure.client.exception.AccountServiceBusinessException;
import com.pitisha.project.mybank.transactionservice.infrastructure.client.exception.AccountServiceTechnicalException;
import feign.FeignException.Forbidden;
import feign.FeignException.Unauthorized;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionOrchestrator {

    private static final String START_RESERVATION = "Starting reservation for tx={}";
    private static final String START_WITHDRAW = "Starting withdrawing for tx={}";
    private static final String START_DEPOSIT = "Starting deposit for tx={}";
    private static final String DEPOSIT_FAILED_BY_TECH = "Deposit failed for tx={} due to technical problem";
    private static final String DEPOSIT_FAILED = "Deposit failed for tx={}";
    private static final String WITHDRAW_FAILED_BY_TECH = "Withdraw failed for tx={} due to technical problem";
    private static final String WITHDRAW_FAILED = "Withdraw failed for tx={}";
    private static final String TRANSFER_FAILED_BY_TECH = "Transfer failed for tx={} due to technical problem";
    private static final String TRANSFER_FAILED = "Transfer failed for tx={}";
    private static final String START_CANCELING = "Initializing canceling for tx={}";
    private static final String CANCELING_FAILED = "Canceling failed for tx={}";
    private static final String DEPOSIT_COMPLETED = "Deposit completed for tx={}";
    private static final String WITHDRAW_COMPLETED = "Withdraw completed for tx={}";
    private static final String TRANSFER_COMPLETED = "Transfer completed for tx={}";
    private static final String TRANSACTION_NOT_DEFINED = "Tx=%s is not defined";
    private static final String TRANSACTION_FAILED = "Tx={} is failed";
    private static final String SKIP_CANCEL = "Skip cancel. Tx={} is already terminated";
    private static final String TRANSACTION_TYPE_IS_NOT_DEFINED = "Transaction type is not defined";

    private final AccountClient accountClient;
    private final TransactionStatusService transactionStatusService;
    private final TransactionRepository transactionRepository;

    @Async("transactionOrchestrationExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleTransaction(final TransactionCreatedEvent event) {
        final TransactionEntity entity = findTx(event.getTransactionId());
        if (entity.getStatus() != NEW) {
            return;
        }
        switch (entity.getType()) {
            case DEPOSIT -> executeDeposit(entity.getId());
            case WITHDRAW -> executeWithdraw(entity.getId());
            case TRANSFER -> executeTransfer(entity.getId());
            default -> throw new IllegalStateException(TRANSACTION_TYPE_IS_NOT_DEFINED);
        }
    }

    public void executeDeposit(final UUID txId) {
        try {
            credit(txId);
            log.info(DEPOSIT_COMPLETED, txId);
        } catch (Unauthorized | Forbidden | AccountServiceBusinessException e) {
            log.warn(DEPOSIT_FAILED, txId, e);
            transactionStatusService.updateStatus(txId, FAILED);
        } catch (AccountServiceTechnicalException e) {
            log.warn(DEPOSIT_FAILED_BY_TECH, txId, e);
            transactionStatusService.updateStatus(txId, FAILED);
            throw e;
        }
    }

    public void executeWithdraw(final UUID txId) {
        try {
            reserve(txId);
            withdraw(txId, true);
            log.info(WITHDRAW_COMPLETED, txId);
        } catch (Unauthorized | Forbidden e) {
            log.warn(WITHDRAW_FAILED_BY_TECH, txId, e);
            transactionStatusService.updateStatus(txId, FAILED);
        } catch (AccountServiceBusinessException e) {
            log.warn(WITHDRAW_FAILED, txId, e);
            canceling(txId, e);
        } catch (AccountServiceTechnicalException e) {
            log.warn(WITHDRAW_FAILED_BY_TECH, txId, e);
            transactionStatusService.updateStatus(txId, FAILED);
            throw e;
        }
    }

    public void executeTransfer(final UUID txId) {
        try {
            reserve(txId);
            withdraw(txId, false);
            credit(txId);
            log.info(TRANSFER_COMPLETED, txId);
        } catch (Unauthorized | Forbidden e) {
            log.error(TRANSFER_FAILED_BY_TECH, txId, e);
            transactionStatusService.updateStatus(txId, FAILED);
        } catch (AccountServiceBusinessException e) {
            log.warn(TRANSFER_FAILED, txId, e);
            canceling(txId, e);
        } catch (AccountServiceTechnicalException e) {
            log.warn(WITHDRAW_FAILED_BY_TECH, txId, e);
            transactionStatusService.updateStatus(txId, FAILED);
            throw e;
        }
    }

    private void reserve(final UUID txId) {
        final TransactionEntity transaction = findTx(txId);
        log.info(START_RESERVATION, transaction.getId());
        accountClient.reserve(
                resolveInitiator(transaction.getInitiator()),
                transaction.getFromAccountId(),
                transaction.getId(),
                new AmountRequest(transaction.getAmount(), transaction.getCurrency())
        );
        transactionStatusService.updateStatus(transaction.getId(), RESERVED);
    }

    private void withdraw(final UUID txId, final boolean terminated) {
        log.info(START_WITHDRAW, txId);
        accountClient.withdraw(txId);
        transactionStatusService.updateStatus(txId, terminated ? COMPLETED : APPLIED);
    }

    private void credit(final UUID txId) {
        final TransactionEntity transaction = findTx(txId);
        log.info(START_DEPOSIT, transaction.getId());
        accountClient.credit(
                resolveInitiator(transaction.getInitiator()),
                transaction.getToAccountId(),
                transaction.getId(),
                new AmountRequest(transaction.getAmount(), transaction.getCurrency())
        );
        transactionStatusService.updateStatus(transaction.getId(), COMPLETED);
    }

    private void canceling(final UUID txId, final Throwable t) {
        final TransactionStatus status = transactionStatusService.findStatusByTransactionId(txId)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_NOT_DEFINED.formatted(txId)));
        if (status == NEW || status == APPLIED) {
            logFailedAndUpdateStatus(txId, t);
            return;
        }
        if (!status.canTransitionTo(CANCELED)) {
            log.info(SKIP_CANCEL, txId);
            return;
        }
        try {
            log.warn(START_CANCELING, txId);
            accountClient.cancel(txId);
            transactionStatusService.updateStatus(txId, CANCELED);
        } catch (Exception e) {
            log.error(CANCELING_FAILED, txId, e);
            logFailedAndUpdateStatus(txId, e);
        }
    }

    private TransactionEntity findTx(final UUID txId) {
        return transactionRepository.findById(txId)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_NOT_DEFINED.formatted(txId)));
    }

    private void logFailedAndUpdateStatus(final UUID txId, final Throwable t) {
        log.warn(TRANSACTION_FAILED, txId, t);
        transactionStatusService.updateStatus(txId, FAILED);
    }

    private String resolveInitiator(final UUID initiator) {
        return nonNull(initiator) ? initiator.toString() : null;
    }
}
