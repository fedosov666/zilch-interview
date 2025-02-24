package com.zilch.payment.application.verification;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.application.verification.verifier.PaymentVerifier;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import com.zilch.payment.domain.verification.events.PaymentVerificationCompletedEvent;
import com.zilch.payment.domain.verification.events.ReadyForVerificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentVerificationRunnerTests {

    private static final PaymentVerification PAYMENT_VERIFICATION = PaymentFactory.createPaymentVerification(
            builder -> builder.verificationType(VerificationType.FRAUD_CHECK));
    private static final Payment PAYMENT =  PaymentFactory.createPayment(paymentBuilder ->
            paymentBuilder
                    .id(PAYMENT_VERIFICATION.paymentId())
                    .paymentVerifications(Collections.singletonList(PAYMENT_VERIFICATION))
    );
    private static final ReadyForVerificationEvent EVENT =
            ReadyForVerificationEvent.builder()
                    .payment(PAYMENT)
                    .paymentVerification(PAYMENT_VERIFICATION)
                    .build();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentVerifier paymentVerifier;

    private PaymentVerificationRunner runner;

    @BeforeEach
    void setUp() {
        when(paymentVerifier.type()).thenReturn(VerificationType.FRAUD_CHECK);
        runner = new PaymentVerificationRunner(List.of(paymentVerifier), eventPublisher);
    }

    @Test
    @DisplayName("Run verification successfully")
    void testRunVerification_success() {
        // Given
        when(paymentVerifier.verify(PAYMENT)).thenReturn(PaymentVerificationStatus.PASSED);

        // When
        runner.runVerification(EVENT);

        // Then
        ArgumentCaptor<PaymentVerificationCompletedEvent> captor =
                ArgumentCaptor.forClass(PaymentVerificationCompletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        PaymentVerificationCompletedEvent publishedEvent = captor.getValue();

        assertThat(publishedEvent.verificationId()).isEqualTo(PAYMENT_VERIFICATION.id());
        assertThat(publishedEvent.paymentId()).isEqualTo(PAYMENT_VERIFICATION.paymentId());
        assertThat(publishedEvent.verificationResult()).isEqualTo(PaymentVerificationStatus.PASSED);
    }

    @Test
    @DisplayName("Handle exception inside verifier")
    void testRunVerification_verifierThrowsException() {
        // Given
        when(paymentVerifier.verify(PAYMENT)).thenThrow(new RuntimeException("Verification error"));

        // When & Then
        assertThatThrownBy(() -> runner.runVerification(EVENT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Verification error");

        ArgumentCaptor<PaymentVerificationCompletedEvent> captor =
                ArgumentCaptor.forClass(PaymentVerificationCompletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        PaymentVerificationCompletedEvent publishedEvent = captor.getValue();

        assertThat(publishedEvent.verificationId()).isEqualTo(PAYMENT_VERIFICATION.id());
        assertThat(publishedEvent.paymentId()).isEqualTo(PAYMENT_VERIFICATION.paymentId());
        assertThat(publishedEvent.verificationResult()).isEqualTo(PaymentVerificationStatus.ERROR);
    }

    @Test
    @DisplayName("Throw an error when cannot find proper verifier")
    void testRunVerification_unknownVerifier() {
        // Given
        PaymentVerification paymentVerification = PaymentFactory.createPaymentVerification(builder -> builder.verificationType(VerificationType.ACCOUNT_STATUS_CHECK));
        Payment payment = PaymentFactory.createPayment(paymentBuilder ->
                paymentBuilder.id(paymentVerification.paymentId()).paymentVerifications(Collections.singletonList(paymentVerification))
        );
        ReadyForVerificationEvent event = ReadyForVerificationEvent.builder()
                .payment(payment)
                .paymentVerification(paymentVerification)
                .build();

        // When & Then
        assertThatThrownBy(() -> runner.runVerification(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot find verifier for: ACCOUNT_STATUS_CHECK");
    }
}
