package com.pitisha.project.mybank.accountservice.domain.service;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

import java.math.BigDecimal;

public interface ExchangeService {

    BigDecimal exchange(BigDecimal amount, AccountCurrency from, AccountCurrency to);
}
