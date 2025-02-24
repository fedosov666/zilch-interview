package com.zilch.payment.domain.verification;

import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import lombok.Builder;

@Builder
public record PaymentVerification(
        Long id,
        String paymentId,
        VerificationType verificationType,
        PaymentVerificationStatus verificationStatus
) {}
