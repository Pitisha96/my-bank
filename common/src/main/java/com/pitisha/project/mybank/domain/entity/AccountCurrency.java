package com.pitisha.project.mybank.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "account currency", description = "currency")
public enum AccountCurrency {
    BYN,
    USD,
    EUR,
    RUB
}
