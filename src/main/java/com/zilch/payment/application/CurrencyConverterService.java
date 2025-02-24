package com.zilch.payment.application;

import com.zilch.payment.domain.money.CurrencyConversion;
import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.enums.Currency;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class CurrencyConverterService implements CurrencyConversion {

    private final ExchangeRateProvider exchangeRateProvider;

    @Override
    public Money convert(Money from, Currency targetCurrency) {
        if (from.currency() == targetCurrency) {
            return from;
        }
        BigDecimal rate = exchangeRateProvider.getExchangeRate(from.currency(), targetCurrency);
        BigDecimal converted = from.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return Money.builder().currency(targetCurrency).amount(converted).build();
    }

    public interface ExchangeRateProvider {
        BigDecimal getExchangeRate(Currency from, Currency to);
    }
}
