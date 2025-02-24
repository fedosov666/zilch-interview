package com.zilch.payment.application;

import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.enums.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyConverterServiceTests {

    @Mock
    private CurrencyConverterService.ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private CurrencyConverterService currencyConverterService;

    @Test
    @DisplayName("Should return same money when converting to the same currency")
    public void testConvert_SameCurrency_ReturnsSameMoney() {
        // Given
        Money money = Money.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.USD)
                .build();

        // When
        Money result = currencyConverterService.convert(money, Currency.USD);

        // Then
        assertThat(result).as("Money should be returned unchanged when target currency is the same").isEqualTo(money);
        verifyNoInteractions(exchangeRateProvider);
    }

    @Test
    @DisplayName("Should convert money to the target currency")
    public void testConvert_DifferentCurrency_ReturnsConvertedMoney() {
        // Given
        Money money = Money.builder()
                .amount(BigDecimal.valueOf(200.00))
                .currency(Currency.USD)
                .build();
        Currency targetCurrency = Currency.EUR;
        BigDecimal rate = BigDecimal.valueOf(0.85);
        when(exchangeRateProvider.getExchangeRate(Currency.USD, Currency.EUR)).thenReturn(rate);

        // When
        Money result = currencyConverterService.convert(money, targetCurrency);

        // Then
        BigDecimal expectedAmount = money.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        assertThat(result.amount()).as("Converted amount should be multiplied by rate and rounded correctly").isEqualTo(expectedAmount);
        assertThat(result.currency()).as("Currency should be updated to target currency").isEqualTo(targetCurrency);
        verify(exchangeRateProvider).getExchangeRate(Currency.USD, Currency.EUR);
    }

    @Test
    @DisplayName("Should round converted amount using HALF_UP")
    public void testConvert_Rounding_HalfUp() {
        // Given
        Money money = Money.builder()
                .amount(BigDecimal.valueOf(10.00))
                .currency(Currency.USD)
                .build();
        Currency targetCurrency = Currency.EUR;
        BigDecimal rate = BigDecimal.valueOf(1.2345);
        when(exchangeRateProvider.getExchangeRate(Currency.USD, Currency.EUR)).thenReturn(rate);

        // When
        Money result = currencyConverterService.convert(money, targetCurrency);

        // Then
        BigDecimal expectedAmount = money.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        assertThat(result.amount()).as("Amount should be rounded using HALF_UP to two decimals").isEqualTo(expectedAmount);
        assertThat(result.currency()).as("Currency should be converted to the target currency").isEqualTo(targetCurrency);
        verify(exchangeRateProvider).getExchangeRate(Currency.USD, Currency.EUR);
    }

    @Test
    @DisplayName("Should convert negative amount")
    public void testConvert_NegativeAmount() {
        // Given
        Money money = Money.builder()
                .amount(BigDecimal.valueOf(-100.00))
                .currency(Currency.USD)
                .build();
        Currency targetCurrency = Currency.EUR;
        BigDecimal rate = BigDecimal.valueOf(0.9);
        when(exchangeRateProvider.getExchangeRate(Currency.USD, Currency.EUR)).thenReturn(rate);

        // When
        Money result = currencyConverterService.convert(money, targetCurrency);

        // Then
        BigDecimal expectedAmount = money.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        assertThat(result.amount()).as("Negative amount should be converted correctly").isEqualTo(expectedAmount);
        assertThat(result.currency()).as("Currency should be converted to the target currency").isEqualTo(targetCurrency);
        verify(exchangeRateProvider).getExchangeRate(Currency.USD, Currency.EUR);
    }
}
