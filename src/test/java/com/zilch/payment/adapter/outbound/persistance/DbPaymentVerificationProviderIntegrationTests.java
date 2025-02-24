package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("h2")
class DbPaymentVerificationProviderIntegrationTests {

    @Autowired
    private DbPaymentVerificationProvider dbPaymentVerificationProvider;

    @Autowired
    private PaymentVerificationRepository paymentVerificationRepository;

    @Test
    @DisplayName("Test save payment verification")
    void testSave() {
        // Given
        PaymentVerification paymentVerification = PaymentFactory.createPaymentVerification(null);

        // When
        PaymentVerification result = dbPaymentVerificationProvider.save(paymentVerification);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.paymentId()).isEqualTo(paymentVerification.paymentId());
        assertThat(result.verificationType()).isEqualTo(paymentVerification.verificationType());
        assertThat(result.verificationStatus()).isEqualTo(paymentVerification.verificationStatus());
    }

    @Test
    @DisplayName("Test update payment verification status")
    void testUpdateStatus() {
        // Given
        PaymentVerification paymentVerification = PaymentFactory.createPaymentVerification(null);
        PaymentVerificationEntity paymentVerificationEntity = PaymentVerificationEntity.fromPaymentVerification(paymentVerification);
        Long verificationId = paymentVerificationRepository.save(paymentVerificationEntity).getId();

        // When
        dbPaymentVerificationProvider.updateStatus(verificationId, PaymentVerificationStatus.PASSED);

        // Then
        Optional<PaymentVerificationEntity> updatedEntity = paymentVerificationRepository.findById(verificationId);
        assertThat(updatedEntity).isPresent();
        assertThat(updatedEntity.get().getVerificationStatus()).isEqualTo(PaymentVerificationStatus.PASSED);
    }
}
