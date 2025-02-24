package com.zilch.payment.application;

import com.zilch.payment.application.verification.PaymentVerificationScheduler;
import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.FuturePaymentDetails;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentVerificationScheduler paymentVerificationScheduler;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("When initializing payment, should properly initialize payment and schedule verifications")
    void initializePayment_ShouldSaveAndScheduleVerification() {
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

        Payment newPayment = Payment.builder()
                .money(Money.builder().amount(amount).currency(currency).build())
                .paymentMethod(paymentMethod)
                .merchant(merchant)
                .paymentStatus(PaymentStatus.NEW)
                .paymentVerifications(Collections.emptyList())
                .build();

        when(paymentProvider.save(any(Payment.class))).thenReturn(newPayment);

        // When
        Payment result = paymentService.initializePayment(futurePaymentDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(newPayment);
        verify(paymentProvider).save(newPayment);
        verify(paymentVerificationScheduler).scheduleVerifications(newPayment);
    }

    @Test
    @DisplayName("When retrieving payment, then should return correct payment")
    void retrieveById_ShouldReturnPayment_WhenFound() {
        // Given
        String paymentId = "12345";
        Payment payment = mock(Payment.class);
        when(paymentProvider.getById(paymentId)).thenReturn(Optional.of(payment));

        // When
        Payment result = paymentService.retrieveById(paymentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(payment);
        verify(paymentProvider).getById(paymentId);
    }

    @Test
    @DisplayName("When retrieving payment, should throw exception when not found")
    void retrieveById_ShouldThrowException_WhenNotFound() {
        // Given
        String paymentId = "invalid";
        when(paymentProvider.getById(paymentId)).thenReturn(Optional.empty());

        // When & Assert
        assertThatThrownBy(() -> paymentService.retrieveById(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Could not find payment by invalid id");
        verify(paymentProvider).getById(paymentId);
    }
}
