package com.zilch.payment.domain.money;

import com.zilch.payment.domain.payment.enums.Currency;

public interface CurrencyConversion {
    Money convert(Money from, Currency targetCurrency);
}
