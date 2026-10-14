package com.example.cgi.dto;

import com.example.cgi.entity.AccountBalance;
import com.example.cgi.model.Currency;

import java.math.BigDecimal;
import java.util.List;

public record OperationResponse(
        boolean success,
        String message,
        OperationDetails operation,
        List<AccountBalance> balances
) {
    public record OperationDetails(
            String type,
            Currency currency,
            BigDecimal amount,
            Currency currencyFrom,
            BigDecimal amountFrom,
            Currency currencyTo,
            BigDecimal amountTo,
            BigDecimal rate
    ) {}
}
