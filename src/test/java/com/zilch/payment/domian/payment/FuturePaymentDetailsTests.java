package com.zilch.payment.domian.payment;

import com.zilch.payment.domain.payment.FuturePaymentDetails;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class FuturePaymentDetailsTests {
    @Test
    @DisplayName("Should correctly convert to payment")
    void toNewPayment_ShouldReturnPaymentWithCorrectFields() {
        // Given
        BigDecimal amount = new BigDecimal("100.50");
        Currency currency = Currency.EUR;
        PaymentMethod paymentMethod = PaymentMethod.PAY_OVER_3_MONTHS;
        String merchant = "TestMerchant";

        FuturePaymentDetails futurePaymentDetails = FuturePaymentDetails.builder()
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .merchant(merchant)
                .build();

        // When
        Payment payment = futurePaymentDetails.toNewPayment();

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.money().amount()).isEqualTo(amount);
        assertThat(payment.money().currency()).isEqualTo(currency);
        assertThat(payment.paymentMethod()).isEqualTo(paymentMethod);
        assertThat(payment.merchant()).isEqualTo(merchant);
        assertThat(payment.paymentStatus()).isEqualTo(PaymentStatus.NEW);
    }
}
