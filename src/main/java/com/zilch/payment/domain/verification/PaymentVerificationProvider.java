package com.zilch.payment.domain.verification;

import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;

public interface PaymentVerificationProvider {
    PaymentVerification save(PaymentVerification paymentVerification);
    void updateStatus(Long id, PaymentVerificationStatus paymentVerificationStatus);
}
