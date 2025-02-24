package com.zilch.payment.application.verification.verifier;

import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;

public interface PaymentVerifier {
    VerificationType type();
    boolean shouldVerify(Payment payment);
    PaymentVerificationStatus verify(Payment payment);
}

