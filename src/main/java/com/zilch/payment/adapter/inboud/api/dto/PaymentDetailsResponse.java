package com.zilch.payment.adapter.inboud.api.dto;

import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;

import java.math.BigDecimal;
import java.util.List;

public record PaymentDetailsResponse(
        String id,
        BigDecimal amount,
        Currency currency,
        PaymentMethod paymentMethod,
        String merchant,
        PaymentStatus paymentStatus,
        List<Verification> verifications
) {

    public static PaymentDetailsResponse fromPayment(Payment payment) {
        return new PaymentDetailsResponse(
                payment.id(),
                payment.money().amount(),
                payment.money().currency(),
                payment.paymentMethod(),
                payment.merchant(),
                payment.paymentStatus(),
                payment.paymentVerifications().stream()
                        .map(verification -> new Verification(
                                verification.verificationType(),
                                verification.verificationStatus()
                        ))
                        .toList()
        );
    }

    public record Verification(
            VerificationType verificationType,
            PaymentVerificationStatus verificationStatus
    ) {}
}
