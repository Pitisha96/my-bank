package com.pitisha.project.mybank.transactionservice.api.dto.response;

import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionStatus;
import com.pitisha.project.mybank.transactionservice.domain.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "transaction", description = "transaction")
public record TransactionResponse(

        @Schema(description = "provided unique identifier", example = "b0de8324-30c5-4d6d-a34a-ad24d8c466d6")
        UUID id,

        @Schema(description = "provided unique identifier of initiator", example = "39ab3931-6303-4022-9ada-932768bc3e3e")
        UUID initiator,

        @Schema(implementation = TransactionType.class, description = "provided transaction types")
        TransactionType type,

        @Schema(implementation = TransactionStatus.class, description = "provided transaction status")
        TransactionStatus status,

        @Schema(description = "provides the account ID from which the funds will be debited", example = "bb722707-0bf4-4d18-b6fd-157d63778a5a")
        UUID accountFrom,

        @Schema(description = "provides the account ID to which the funds will be credited", example = "f5e12f33-d60b-4b85-879c-2f03cdcee483")
        UUID accountTo,

        @Schema(description = "provides amount", example = "120.20")
        BigDecimal amount,

        @Schema(description = "provides creation time", example = "2025-12-01T00:00:00")
        LocalDateTime createdAt,

        @Schema(description = "provides update time", example = "2026-01-10T00:00:00")
        LocalDateTime updatedAt
) { }
