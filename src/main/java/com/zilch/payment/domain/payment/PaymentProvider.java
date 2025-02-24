package com.zilch.payment.domain.payment;

import com.zilch.payment.domain.payment.enums.PaymentStatus;

import java.util.Optional;

public interface PaymentProvider {
    Payment save(Payment payment);
    Optional<Payment> getById(String id);
    void setPaymentStatus(String paymentId, PaymentStatus paymentStatus);
}
