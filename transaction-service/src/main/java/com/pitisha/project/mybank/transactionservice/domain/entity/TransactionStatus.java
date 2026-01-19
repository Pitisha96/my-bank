package com.pitisha.project.mybank.transactionservice.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "transaction status")
public enum TransactionStatus {
    NEW,
    RESERVED,
    APPLIED,
    COMPLETED,
    CANCELED,
    FAILED;

    public boolean canTransitionTo(final TransactionStatus other) {
        return switch (this) {
            case NEW -> other == RESERVED || other == COMPLETED || other == FAILED;
            case RESERVED -> other == APPLIED || other == COMPLETED || other == CANCELED || other == FAILED;
            case APPLIED -> other == COMPLETED || other == FAILED;
            case COMPLETED, CANCELED, FAILED -> false;
        };
    }

}
