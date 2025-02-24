package com.zilch.payment.application;

import com.zilch.payment.domain.payment.enums.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StaticRateCurrencyProviderTests {

    private final StaticRateCurrencyProvider provider = new StaticRateCurrencyProvider();

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, 0.92",
            "EUR, USD, 1.09",
            "USD, GBP, 0.78",
            "GBP, USD, 1.28",
            "EUR, GBP, 0.85",
            "GBP, EUR, 1.18"
    })
    @DisplayName("Should return correct exchange rate")
    void testGetExchangeRate(Currency from, Currency to, BigDecimal expectedRate) {
        BigDecimal rate = provider.getExchangeRate(from, to);
        assertThat(rate).isEqualByComparingTo(expectedRate);
    }

}
