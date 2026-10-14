package com.example.cgi.dto;

import com.example.cgi.model.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ExchangeRequest(
        @NotNull Currency currencyFrom,
        @NotNull Currency currencyTo,

        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal amount
) {}