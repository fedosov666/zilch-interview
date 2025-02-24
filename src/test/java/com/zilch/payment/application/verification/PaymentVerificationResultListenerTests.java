package com.zilch.payment.application.verification;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentNotFoundException;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.PaymentVerificationProvider;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.events.PaymentVerificationCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentVerificationResultListenerTests {

    private static final String PAYMENT_ID = UUID.randomUUID().toString();

    private static final PaymentVerification PAYMENT_VERIFICATION_PASSED_1 =
            PaymentFactory.createPaymentVerification(builder -> builder
                    .paymentId(PAYMENT_ID)
                    .verificationStatus(PaymentVerificationStatus.PASSED));

    private static final PaymentVerification PAYMENT_VERIFICATION_PASSED_2 =
            PaymentFactory.createPaymentVerification(builder -> builder
                    .paymentId(PAYMENT_ID)
                    .verificationStatus(PaymentVerificationStatus.PASSED));

    private static final PaymentVerification PAYMENT_VERIFICATION_SCHEDULED =
            PaymentFactory.createPaymentVerification(builder -> builder
                    .paymentId(PAYMENT_ID)
                    .verificationStatus(PaymentVerificationStatus.SCHEDULED));

    private static final PaymentVerification PAYMENT_VERIFICATION_FAILED =
            PaymentFactory.createPaymentVerification(builder -> builder
                    .paymentId(PAYMENT_ID)
                    .verificationStatus(PaymentVerificationStatus.FAILED));

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentVerificationProvider paymentVerificationProvider;

    @InjectMocks
    private PaymentVerificationResultListener listener;

    @Test
    @DisplayName("Set payment status to ACCEPTED when all verifications passed")
    void processPaymentVerificationResult_whenPassedAndAllVerificationsPassed_thenSetAcceptedStatus() {
        // Given
        PaymentVerificationCompletedEvent event = PaymentVerificationCompletedEvent.builder()
                .verificationId(PAYMENT_VERIFICATION_PASSED_2.id())
                .paymentId(PAYMENT_ID)
                .verificationResult(PaymentVerificationStatus.PASSED)
                .build();

        Payment payment = createPaymentWithVerifications(
                asList(PAYMENT_VERIFICATION_PASSED_1, PAYMENT_VERIFICATION_PASSED_2));
        when(paymentProvider.getById(payment.id())).thenReturn(Optional.of(payment));

        // When
        listener.processPaymentVerificationResult(event);

        // Then
        verify(paymentVerificationProvider)
                .updateStatus(PAYMENT_VERIFICATION_PASSED_2.id(), PaymentVerificationStatus.PASSED);
        verify(paymentProvider).setPaymentStatus(PAYMENT_ID, PaymentStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Do not set payment status to ACCEPTED when not all verifications passed")
    void processPaymentVerificationResult_whenPassedButNotAllVerificationsPassed_thenDoNotSetAcceptedStatus() {
        // Given
        PaymentVerificationCompletedEvent event = PaymentVerificationCompletedEvent.builder()
                .verificationId(PAYMENT_VERIFICATION_PASSED_2.id())
                .paymentId(PAYMENT_ID)
                .verificationResult(PaymentVerificationStatus.PASSED)
                .build();

        Payment payment = createPaymentWithVerifications(
                asList(PAYMENT_VERIFICATION_PASSED_1, PAYMENT_VERIFICATION_PASSED_2, PAYMENT_VERIFICATION_SCHEDULED));
        when(paymentProvider.getById(payment.id())).thenReturn(Optional.of(payment));
        when(paymentProvider.getById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // When
        listener.processPaymentVerificationResult(event);

        // Then
        verify(paymentVerificationProvider)
                .updateStatus(PAYMENT_VERIFICATION_PASSED_2.id(), PaymentVerificationStatus.PASSED);
        verify(paymentProvider, never()).setPaymentStatus(anyString(), any());
    }

    @ParameterizedTest
    @EnumSource(value = PaymentVerificationStatus.class, names = {"FAILED", "ERROR"})
    @DisplayName("Set payment status to REJECTED when any verification is not passed")
    void processPaymentVerificationResult_whenVerificationNotPassed_thenSetRejectedStatus(PaymentVerificationStatus status) {
        // Given
        PaymentVerificationCompletedEvent event = PaymentVerificationCompletedEvent.builder()
                .verificationId(PAYMENT_VERIFICATION_FAILED.id())
                .paymentId(PAYMENT_ID)
                .verificationResult(status)
                .build();

        Payment payment = createPaymentWithVerifications(
                asList(PAYMENT_VERIFICATION_PASSED_1, PAYMENT_VERIFICATION_PASSED_2, PAYMENT_VERIFICATION_FAILED));
        when(paymentProvider.getById(payment.id())).thenReturn(Optional.of(payment));
        when(paymentProvider.getById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // When
        listener.processPaymentVerificationResult(event);

        // Then
        verify(paymentVerificationProvider)
                .updateStatus(PAYMENT_VERIFICATION_FAILED.id(), status);
        verify(paymentProvider).setPaymentStatus(PAYMENT_ID, PaymentStatus.REJECTED);
    }

    @Test
    @DisplayName("Throw exception when payment not found")
    void processPaymentVerificationResult_whenPaymentNotFound_thenThrowException() {
        // Given
        PaymentVerificationCompletedEvent event = PaymentVerificationCompletedEvent.builder()
                .verificationId(PAYMENT_VERIFICATION_PASSED_1.id())
                .paymentId(PAYMENT_ID)
                .verificationResult(PaymentVerificationStatus.PASSED)
                .build();
        when(paymentProvider.getById(PAYMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> listener.processPaymentVerificationResult(event))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    private static Payment createPaymentWithVerifications(List<PaymentVerification> paymentVerificationList) {
        return PaymentFactory.createPayment(builder -> builder
                .id(PAYMENT_ID)
                .money(Money.builder().amount(BigDecimal.valueOf(200.0)).currency(Currency.EUR).build())
                .paymentVerifications(paymentVerificationList));
    }
}
