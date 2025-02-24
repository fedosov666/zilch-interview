package com.zilch.payment.adapter.inboud.api.dto;

import com.zilch.payment.domain.payment.FuturePaymentDetails;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InitializePaymentRequestTests {
    @Test
    @DisplayName("Should convert to FuturePaymentDetails")
    void testToFuturePaymentDetails() {
        // Given
        InitializePaymentRequest request = InitializePaymentRequest.builder()
                .amount(BigDecimal.valueOf(100.00))
                .currency(Currency.USD)
                .paymentMethod(PaymentMethod.PAY_NOW)
                .merchant("Test Merchant")
                .build();

        // When
        FuturePaymentDetails futurePaymentDetails = request.toFuturePaymentDetails();

        // Then
        assertThat(futurePaymentDetails.amount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(futurePaymentDetails.currency()).isEqualTo(Currency.USD);
        assertThat(futurePaymentDetails.paymentMethod()).isEqualTo(PaymentMethod.PAY_NOW);
        assertThat(futurePaymentDetails.merchant()).isEqualTo("Test Merchant");
    }
}
