package com.zilch.payment.domain.verification.events;

import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import lombok.Builder;

@Builder
public record PaymentVerificationCompletedEvent(
        Long verificationId,
        String paymentId,
        PaymentVerificationStatus verificationResult
) {}
