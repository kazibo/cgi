package com.example.cgi.dto;

import com.example.cgi.model.Currency;

import java.math.BigDecimal;

public sealed interface OperationDetails {

    record Deposit(
            Currency currency,
            BigDecimal amount
    ) implements OperationDetails {}

    record Withdraw(
            Currency currency,
            BigDecimal amount
    ) implements OperationDetails {}

    record Exchange(
            Currency currencyFrom,
            BigDecimal amountFrom,
            Currency currencyTo,
            BigDecimal amountTo,
            BigDecimal rate
    ) implements OperationDetails {}
}