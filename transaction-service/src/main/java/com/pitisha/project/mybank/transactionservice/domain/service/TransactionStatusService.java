package com.pitisha.project.mybank.transactionservice.domain.service;

import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus;

import java.util.Optional;
import java.util.UUID;

public interface TransactionStatusService {

    Optional<TransactionStatus> findStatusByTransactionId(UUID id);
    void updateStatus(UUID txId, TransactionStatus status);
}
