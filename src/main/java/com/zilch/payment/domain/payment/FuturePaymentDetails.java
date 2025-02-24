package com.zilch.payment.domain.payment;

import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Collections;

@Builder
public record FuturePaymentDetails(
        BigDecimal amount,
        Currency currency,
        PaymentMethod paymentMethod,
        String merchant
) {
    public Payment toNewPayment() {
        return Payment.builder()
                .money(Money.builder().amount(amount).currency(currency).build())
                .paymentMethod(paymentMethod)
                .merchant(merchant)
                .paymentStatus(PaymentStatus.NEW)
                .paymentVerifications(Collections.emptyList())
                .build();
    }
}
