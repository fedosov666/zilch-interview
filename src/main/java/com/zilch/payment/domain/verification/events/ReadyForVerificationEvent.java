package com.zilch.payment.domain.verification.events;

import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.verification.PaymentVerification;
import lombok.Builder;

@Builder
public record ReadyForVerificationEvent(
        Payment payment,
        PaymentVerification paymentVerification
) {}
