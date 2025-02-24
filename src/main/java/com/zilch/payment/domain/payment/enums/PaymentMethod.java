package com.zilch.payment.domain.payment.enums;

public enum PaymentMethod {
    PAY_NOW(false),
    PAY_OVER_3_MONTHS(true),
    PAY_OVER_6_MONTHS(true);

    public final boolean isPayLaterMethod;

    PaymentMethod(boolean isPayLaterMethod) {
        this.isPayLaterMethod = isPayLaterMethod;
    }
}