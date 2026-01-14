package com.pitisha.project.mybank.transactionservice.api.dto.response;

import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID initiator,
        TransactionType type,
        TransactionStatus status,
        UUID accountFrom,
        UUID accountTo,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
