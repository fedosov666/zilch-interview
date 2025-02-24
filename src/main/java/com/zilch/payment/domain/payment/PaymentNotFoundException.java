package com.zilch.payment.domain.payment;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String paymentId) {
        super("Could not find payment by " + paymentId + " id");
    }
}
