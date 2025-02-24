package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbPaymentProviderTests {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DbPaymentProvider dbPaymentProvider;

    private static final Payment PAYMENT = PaymentFactory.createPayment(paymentBuilder ->
            paymentBuilder.paymentVerifications(Collections.emptyList()));

    @Test
    @DisplayName("Given valid payment, when saving, then repository should be called and return saved payment")
    void givenValidPayment_whenSaving_thenRepositoryShouldBeCalledAndReturnSavedPayment() {
        // Given
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(PaymentEntity.fromPayment(PAYMENT));

        // When
        Payment savedPayment = dbPaymentProvider.save(PAYMENT);

        // Then
        assertThat(savedPayment).isEqualTo(PAYMENT);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("Given repository throws exception, when saving, then exception should be propagated")
    void givenRepositoryThrowsException_whenSaving_thenExceptionShouldBePropagated() {
        // Given
        when(paymentRepository.save(any(PaymentEntity.class))).thenThrow(new RuntimeException("Database error"));

        // When/Then
        assertThatThrownBy(() -> dbPaymentProvider.save(PAYMENT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @Test
    @DisplayName("Given existing payment id, when retrieving, then return corresponding payment")
    void givenExistingPaymentId_whenRetrieving_thenReturnCorrespondingPayment() {
        // Given
        when(paymentRepository.findById(PAYMENT.id())).thenReturn(Optional.of(PaymentEntity.fromPayment(PAYMENT)));

        // When
        Optional<Payment> retrievedPayment = dbPaymentProvider.getById(PAYMENT.id());

        // Then
        assertThat(retrievedPayment).isPresent().contains(PAYMENT);
    }

    @Test
    @DisplayName("Given non-existing payment id, when retrieving, then return empty optional")
    void givenNonExistingPaymentId_whenRetrieving_thenReturnEmptyOptional() {
        // Given
        when(paymentRepository.findById("unexistingpaymentid")).thenReturn(Optional.empty());

        // When
        Optional<Payment> retrievedPayment = dbPaymentProvider.getById("unexistingpaymentid");

        // Then
        assertThat(retrievedPayment).isEmpty();
    }

    @Test
    @DisplayName("Given valid payment id and status, when updating, then repository should update payment status")
    void givenValidPaymentIdAndStatus_whenUpdating_thenRepositoryShouldUpdatePaymentStatus() {
        // Given
        final String paymentId = "test123";
        doNothing().when(paymentRepository).updatePaymentStatus(paymentId, PaymentStatus.ACCEPTED);

        // When
        dbPaymentProvider.setPaymentStatus(paymentId, PaymentStatus.ACCEPTED);

        // Then
        verify(paymentRepository).updatePaymentStatus(paymentId, PaymentStatus.ACCEPTED);
    }
}

