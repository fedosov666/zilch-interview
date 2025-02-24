package com.zilch.payment.domain.money;

import com.zilch.payment.domain.payment.enums.Currency;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Money(
        BigDecimal amount,
        Currency currency
) {
    public boolean isGreaterThan(Money other) {
        if (this.currency != other.currency) {
            throw new DifferentCurrenciesOnComparisonException(this.currency, other.currency);
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public static class DifferentCurrenciesOnComparisonException extends RuntimeException {
        public DifferentCurrenciesOnComparisonException(Currency from, Currency to) {
            super(String.format("Cannot compare %s and %s currencies without conversion", from, to));
        }
    }
}