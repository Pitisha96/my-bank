package com.pitisha.project.mybank.transactionservice.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "types of transaction")
public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}
