package com.zilch.payment.adapter.inboud.api.dto;

import com.zilch.payment.domain.payment.FuturePaymentDetails;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InitializePaymentRequest(
        @DecimalMin(value = "0.01", message = "Amount must be higher than 0")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "Merchant should be provided")
        String merchant
) {
    public FuturePaymentDetails toFuturePaymentDetails() {
        return FuturePaymentDetails.builder()
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .merchant(merchant)
                .build();
    }
}
