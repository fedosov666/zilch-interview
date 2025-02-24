package com.zilch.payment;

import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class PaymentFactory {

    public static PaymentVerification createPaymentVerification(
            Consumer<PaymentVerification.PaymentVerificationBuilder> overrides) {
        PaymentVerification.PaymentVerificationBuilder builder = PaymentVerification.builder()
                .id(new Random().nextLong())
                .paymentId(UUID.randomUUID().toString())
                .verificationType(VerificationType.FRAUD_CHECK)
                .verificationStatus(PaymentVerificationStatus.SCHEDULED);
        if (overrides != null) {
            overrides.accept(builder);
        }
        return builder.build();
    }

    public static Payment createPayment(Consumer<Payment.PaymentBuilder> overrides) {
        final String paymentId = UUID.randomUUID().toString();
        Payment.PaymentBuilder builder = Payment.builder()
                .id(paymentId)
                .money(Money.builder().amount(BigDecimal.valueOf(100)).currency(Currency.EUR).build())
                .paymentMethod(PaymentMethod.PAY_OVER_3_MONTHS)
                .merchant("Test Merchant")
                .paymentStatus(PaymentStatus.VERIFYING)
                .paymentVerifications(Collections.singletonList(createPaymentVerification(payVer -> payVer.paymentId(paymentId))));
        if (overrides != null) {
            overrides.accept(builder);
        }
        return builder.build();
    }
}
