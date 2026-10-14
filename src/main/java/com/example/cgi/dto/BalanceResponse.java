package com.example.cgi.dto;

import com.example.cgi.model.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

public record BalanceResponse(
        Long accountId,
        Map<Currency, BigDecimal> balances
) {
    @Component
    public static class ExchangeRateProvider {

        private static final Map<Currency, BigDecimal> PER_EUR =
                Map.of(
                        Currency.EUR, new BigDecimal("1.00"),
                        Currency.USD, new BigDecimal("1.10"),
                        Currency.SEK, new BigDecimal("11.00"),
                        Currency.GBP, new BigDecimal("0.85")
                );

        public BigDecimal rate(Currency from, Currency to) {
            BigDecimal fromRate = PER_EUR.get(from);
            BigDecimal toRate = PER_EUR.get(to);

            return toRate.divide(
                    fromRate,
                    8,
                    java.math.RoundingMode.HALF_UP
            );
        }
    }
}