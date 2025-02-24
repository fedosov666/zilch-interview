package com.zilch.payment.application;

import com.zilch.payment.domain.payment.enums.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class StaticRateCurrencyProvider implements CurrencyConverterService.ExchangeRateProvider {

    private static final Map<String, BigDecimal> EXCHANGE_RATES = Map.of(
            "USD_EUR", BigDecimal.valueOf(0.92),
            "EUR_USD", BigDecimal.valueOf(1.09),
            "USD_GBP", BigDecimal.valueOf(0.78),
            "GBP_USD", BigDecimal.valueOf(1.28),
            "EUR_GBP", BigDecimal.valueOf(0.85),
            "GBP_EUR", BigDecimal.valueOf(1.18)
    );

    public BigDecimal getExchangeRate(Currency from, Currency to) {
        String key = from.name() + "_" + to.name();
        if (!EXCHANGE_RATES.containsKey(key)) {
            throw new IllegalArgumentException("Exchange rate not found for " + from + " to " + to);
        }
        return EXCHANGE_RATES.get(key);
    }

}
