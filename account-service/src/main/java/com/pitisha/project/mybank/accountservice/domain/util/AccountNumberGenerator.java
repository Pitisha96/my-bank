package com.pitisha.project.mybank.accountservice.domain.util;

import com.pitisha.project.mybank.domain.entity.AccountCurrency;

public class AccountNumberGenerator {

    private static final String ACCOUNT_NUMBER_FORMAT = "40817%s00000%07d";

    public static String generateAccountNumber(final String bik, final AccountCurrency currency, final Long accountSequence) {
        final String base = ACCOUNT_NUMBER_FORMAT.formatted(currencyCode(currency), accountSequence);
        int checksum = calculateCheckSum(base, bik);
        return base.substring(0, 8) + checksum + base.substring(9);
    }

    private static String currencyCode(final AccountCurrency currency) {
        return switch (currency) {
            case RUB -> "810";
            case USD -> "840";
            case EUR -> "978";
            case BYN -> "933";
        };
    }

    private static int calculateCheckSum(final String account, final String bik) {
        final String lastCheckSum = bik.substring(bik.length() - 3);
        final String full = lastCheckSum + account;
        final int[] coefficients = {7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1};
        int sum = 0;
        for (int i = 0; i < full.length(); i++) {
            sum += Character.getNumericValue(full.charAt(i)) * coefficients[i];
        }
        return sum % 10;
    }
}
