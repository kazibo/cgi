
package com.example.cgi.service;

import com.example.cgi.model.Currency;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class ExchangeRateProvider {

        private final Map<Currency, CurrencyRate> rates;

        public ExchangeRateProvider(ObjectMapper objectMapper) {
                try {
                        ExchangeRatesConfig config = objectMapper.readValue(
                                new ClassPathResource("exchange-rates.json").getInputStream(),
                                ExchangeRatesConfig.class
                        );

                        if (config.rates() == null || config.rates().isEmpty()) {
                                throw new IllegalStateException("Exchange rates are missing");
                        }

                        this.rates = Map.copyOf(config.rates());

                        validateRates(config);
                } catch (IOException e) {
                        throw new IllegalStateException(
                                "Failed to load exchange-rates.json", e
                        );
                }
        }

        /**
         * Returns the amount of target currency received
         * for one unit of source currency.
         */
        public BigDecimal rate(Currency from, Currency to) {
                if (from == to) {
                        return BigDecimal.ONE;
                }

                CurrencyRate source = getRate(from);
                CurrencyRate target = getRate(to);

                // Sell the source currency to the bank at its buy rate,
                // then buy the target currency at its sell rate.
                return target.sell().divide(
                        source.buy(),
                        5,  // Swedbank exchange rates page has this precision
                        RoundingMode.HALF_UP
                );
        }

        private CurrencyRate getRate(Currency currency) {
                CurrencyRate rate = rates.get(currency);

                if (rate == null) {
                        throw new IllegalArgumentException(
                                "Exchange rate not configured for " + currency
                        );
                }

                return rate;
        }

        private void validateRates(ExchangeRatesConfig config) {
                if (config.baseCurrency() != Currency.EUR) {
                        throw new IllegalStateException(
                                "Base currency must be EUR"
                        );
                }

                for (Currency currency : Currency.values()) {
                        CurrencyRate rate = rates.get(currency);

                        if (rate == null
                                || rate.buy() == null
                                || rate.sell() == null
                                || rate.buy().signum() <= 0
                                || rate.sell().signum() <= 0
                                || rate.buy().compareTo(rate.sell()) > 0) {
                                throw new IllegalStateException(
                                        "Invalid exchange rates for " + currency
                                );
                        }
                }

                CurrencyRate eur = rates.get(Currency.EUR);
                if (eur.buy().compareTo(BigDecimal.ONE) != 0
                        || eur.sell().compareTo(BigDecimal.ONE) != 0) {
                        throw new IllegalStateException(
                                "EUR rates must both equal 1"
                        );
                }
        }

        public record ExchangeRatesConfig(
                Currency baseCurrency,
                Map<Currency, CurrencyRate> rates
        ) {}

        public record CurrencyRate(
                BigDecimal buy,
                BigDecimal sell
        ) {}
}
