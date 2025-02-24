package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbPaymentVerificationProviderTests {

    @Mock
    private PaymentVerificationRepository paymentVerificationRepository;

    @InjectMocks
    private DbPaymentVerificationProvider dbPaymentVerificationProvider;

    private static final PaymentVerification PAYMENT_VERIFICATION = PaymentFactory.createPaymentVerification(null);

    @Test
    @DisplayName("Given valid payment verification, when saving, then repository should be called")
    void givenValidPaymentVerification_whenSaving_thenRepositoryShouldBeCalled() {
        // Given
        when(paymentVerificationRepository.save(any(PaymentVerificationEntity.class)))
                .thenReturn(PaymentVerificationEntity.fromPaymentVerification(PAYMENT_VERIFICATION));

        // When
        dbPaymentVerificationProvider.save(PAYMENT_VERIFICATION);

        // Then
        verify(paymentVerificationRepository).save(argThat(entity ->
            entity.getId() == null &&
            entity.getPaymentId().equals(PAYMENT_VERIFICATION.paymentId()) &&
            entity.getVerificationType() == PAYMENT_VERIFICATION.verificationType() &&
            entity.getVerificationStatus() == PAYMENT_VERIFICATION.verificationStatus()
        ));
    }

    @Test
    @DisplayName("Given repository throws exception, when saving, then exception should be propagated")
    void givenRepositoryThrowsException_whenSaving_thenExceptionShouldBePropagated() {
        // Given
        when(paymentVerificationRepository.save(any(PaymentVerificationEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When/Then
        assertThatThrownBy(() -> dbPaymentVerificationProvider.save(PAYMENT_VERIFICATION))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @Test
    @DisplayName("Given valid payment verification id and status, when updating status, then repository should update payment verification status")
    void givenValidPaymentVerificationIdAndStatus_whenUpdating_thenRepositoryShouldUpdatePaymentVerificationStatus() {
        // Given
        final Long verificationId = 1L;
        doNothing().when(paymentVerificationRepository).updatePaymentVerificationStatus(verificationId, PaymentVerificationStatus.PASSED);

        // When
        dbPaymentVerificationProvider.updateStatus(verificationId, PaymentVerificationStatus.PASSED);

        // Then
        verify(paymentVerificationRepository).updatePaymentVerificationStatus(verificationId, PaymentVerificationStatus.PASSED);
    }
}
