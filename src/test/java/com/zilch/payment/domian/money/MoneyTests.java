package com.zilch.payment.domian.money;

import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.enums.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTests {

    @Test
    @DisplayName("Is greater than returns correct result")
    void testIsGreaterThan() {
        Money money1 = Money.builder()
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.USD)
                .build();

        Money money2 = Money.builder()
                .amount(BigDecimal.valueOf(50))
                .currency(Currency.USD)
                .build();

        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isGreaterThan(money1)).isFalse();
    }

    @Test
    @DisplayName("Is greater than throws exception when comparing different currencies")
    void testIsGreaterThanDifferentCurrencies() {
        Money money1 = Money.builder()
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.USD)
                .build();

        Money money2 = Money.builder()
                .amount(BigDecimal.valueOf(50))
                .currency(Currency.EUR)
                .build();

        assertThatThrownBy(() -> money1.isGreaterThan(money2))
                .isInstanceOf(Money.DifferentCurrenciesOnComparisonException.class)
                .hasMessageContaining("Cannot compare USD and EUR currencies without conversion");
    }
}
