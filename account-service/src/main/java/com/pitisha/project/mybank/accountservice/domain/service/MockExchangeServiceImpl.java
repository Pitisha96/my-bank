package com.pitisha.project.mybank.accountservice.domain.service;

import static com.pitisha.project.mybank.domain.entity.AccountCurrency.BYN;
import static com.pitisha.project.mybank.domain.entity.AccountCurrency.EUR;
import static com.pitisha.project.mybank.domain.entity.AccountCurrency.RUB;
import static com.pitisha.project.mybank.domain.entity.AccountCurrency.USD;
import static java.math.RoundingMode.HALF_UP;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MockExchangeServiceImpl implements ExchangeService {

    private static final int SCALE = 6;

    private final Map<AccountCurrency, BigDecimal> exchangeRates = Map.of(
            USD, new BigDecimal("78.7913"),
            BYN, new BigDecimal("26.8986"),
            EUR, new BigDecimal("91.9668")
    );

    @Override
    public BigDecimal exchange(final BigDecimal amount, final AccountCurrency from, final AccountCurrency to) {
        if (from == to) {
            return amount;
        }
        final BigDecimal rubAmount = exchangeToRub(amount, from);
        return exchangeFromRub(rubAmount, to);
    }

    private BigDecimal exchangeToRub(final BigDecimal amount, final AccountCurrency currency) {
        if (currency == RUB) {
            return amount;
        }
        return amount
                .multiply(exchangeRates.get(currency))
                .setScale(SCALE, HALF_UP);
    }

    private BigDecimal exchangeFromRub(final BigDecimal amount, final AccountCurrency currency) {
        if (currency == RUB) {
            return amount;
        }
        return amount.divide(exchangeRates.get(currency), SCALE, HALF_UP);
    }
}
