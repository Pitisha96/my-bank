package com.pitisha.project.mybank.transactionservice.domain.service;

import static java.util.Objects.requireNonNull;

import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionEntity;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus;
import com.pitisha.project.mybank.transactionservice.domain.exception.ResourceNotFoundException;
import com.pitisha.project.mybank.transactionservice.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionStatusServiceImpl implements TransactionStatusService {

    private static final String TRANSACTION_ID_MUST_NOT_BE_NULL = "Transaction id must not be null";
    private static final String STATUS_MUST_NOT_BE_NULL = "Status must not be null";
    private static final String TRANSACTION_NOT_FOUND_MESSAGE = "Tx=%s not found";
    private static final String ILLEGAL_STATUS_TRANSITION = "Illegal status transition {} -> {} for tx={}";

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @Override
    public Optional<TransactionStatus> findStatusByTransactionId(final UUID txId) {
        requireNonNull(txId, TRANSACTION_ID_MUST_NOT_BE_NULL);
        return transactionRepository.findStatusById(txId);
    }

    @Transactional
    @Override
    public void updateStatus(final UUID txId, final TransactionStatus status) {
        requireNonNull(txId, TRANSACTION_ID_MUST_NOT_BE_NULL);
        requireNonNull(status, STATUS_MUST_NOT_BE_NULL);
        final TransactionEntity transaction = transactionRepository.lockForUpdate(txId)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_NOT_FOUND_MESSAGE.formatted(txId)));
        final TransactionStatus current = transaction.getStatus();
        if (current == status) {
            return;
        }
        if (!current.canTransitionTo(status)) {
            log.warn(ILLEGAL_STATUS_TRANSITION, current.name(), status.name(), txId);
            return;
        }
        transaction.setStatus(status);
    }
}
