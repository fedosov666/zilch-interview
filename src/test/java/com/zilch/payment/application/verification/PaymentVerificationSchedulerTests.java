package com.zilch.payment.application.verification;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.application.verification.verifier.PaymentVerifier;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.PaymentVerificationProvider;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import com.zilch.payment.domain.verification.events.ReadyForVerificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentVerificationSchedulerTests {

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentVerificationProvider paymentVerificationProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentVerifier firstTestVerifier;

    @Mock
    private PaymentVerifier secondTestVerifier;

    private PaymentVerificationScheduler paymentVerificationScheduler;

    @BeforeEach
    public void init() {
        List<PaymentVerifier> verifiers = List.of(firstTestVerifier, secondTestVerifier);
        paymentVerificationScheduler = new PaymentVerificationScheduler(
                paymentProvider, paymentVerificationProvider, eventPublisher, verifiers);
    }

    @Test
    @DisplayName("Should accept payment when no verifications are scheduled")
    void scheduleVerifications_ShouldAcceptPayment_WhenNoVerificationsAreScheduled() {
        // Given
        Payment payment = PaymentFactory.createPayment(null);
        when(firstTestVerifier.shouldVerify(payment)).thenReturn(false);
        when(secondTestVerifier.shouldVerify(payment)).thenReturn(false);

        // When
        paymentVerificationScheduler.scheduleVerifications(payment);

        // Then
        verify(paymentProvider).setPaymentStatus(payment.id(), PaymentStatus.ACCEPTED);
        verifyNoInteractions(paymentVerificationProvider, eventPublisher);
    }

    @Test
    @DisplayName("Should schedule verification only for required verifiers")
    void scheduleVerifications_ShouldScheduleVerification_OnlyForRequiredVerifiers() {
        // Given
        Payment payment = PaymentFactory.createPayment(null);
        when(firstTestVerifier.shouldVerify(payment)).thenReturn(false);
        when(secondTestVerifier.shouldVerify(payment)).thenReturn(true);
        when(secondTestVerifier.type()).thenReturn(VerificationType.FRAUD_CHECK);
        PaymentVerification verification = PaymentFactory.createPaymentVerification(builder ->
                builder.paymentId(payment.id())
                        .verificationStatus(PaymentVerificationStatus.SCHEDULED)
                        .verificationType(VerificationType.FRAUD_CHECK)
        );
        when(paymentVerificationProvider.save(any())).thenReturn(verification);

        // When
        paymentVerificationScheduler.scheduleVerifications(payment);

        // Then
        verify(paymentVerificationProvider).save(any());
        verify(eventPublisher).publishEvent(any(ReadyForVerificationEvent.class));
        verify(paymentProvider).setPaymentStatus(payment.id(), PaymentStatus.VERIFYING);
    }

    @Test
    @DisplayName("Should reject payment when exception occurs during verification scheduling")
    void scheduleVerifications_ShouldRejectPayment_WhenExceptionOccurs() {
        // Given
        Payment payment = PaymentFactory.createPayment(null);
        doThrow(new RuntimeException("Test Exception")).when(firstTestVerifier).shouldVerify(payment);

        // When
        paymentVerificationScheduler.scheduleVerifications(payment);

        // Then
        verify(paymentProvider).setPaymentStatus(payment.id(), PaymentStatus.REJECTED);
    }
}

