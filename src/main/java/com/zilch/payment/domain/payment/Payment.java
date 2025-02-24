package com.zilch.payment.domain.payment;

import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerification;
import lombok.Builder;

import java.util.List;

@Builder
public record Payment(
    String id,
    Money money,
    PaymentMethod paymentMethod,
    String merchant,
    PaymentStatus paymentStatus,
    List<PaymentVerification> paymentVerifications
) {}
