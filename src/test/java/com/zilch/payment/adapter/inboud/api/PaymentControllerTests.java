package com.zilch.payment.adapter.inboud.api;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.adapter.inboud.api.dto.InitializePaymentRequest;
import com.zilch.payment.adapter.inboud.api.dto.PaymentDetailsResponse;
import com.zilch.payment.application.PaymentService;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentNotFoundException;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTests {
    @Mock
    private PaymentService paymentService;

    private PaymentController paymentController;

    @BeforeEach
    public void setUp() {
        paymentController = new PaymentController(paymentService);
    }

    @Test
    @DisplayName("Should initialize payment successfully")
    public void testInitializePayment_Success() {
        // Given
        InitializePaymentRequest request = InitializePaymentRequest.builder()
                .amount(new BigDecimal("150.00"))
                .currency(Currency.EUR)
                .paymentMethod(PaymentMethod.PAY_NOW)
                .merchant("test-merchant")
                .build();
        when(paymentService.initializePayment(any())).thenReturn(request.toFuturePaymentDetails().toNewPayment());

        // When
        PaymentDetailsResponse response = paymentController.initializePayment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(request.amount());
        assertThat(response.currency()).isEqualTo(Currency.EUR);
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.PAY_NOW);
        assertThat(response.merchant()).isEqualTo(request.merchant());
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.NEW);
        assertThat(response.verifications()).isEmpty();
    }

    @Test
    @DisplayName("Should get payment successfully")
    public void testGetPayment_Success() {
        // Given
        Payment payment = PaymentFactory.createPayment(null);
        PaymentVerification paymentVerification = payment.paymentVerifications().get(0);
        when(paymentService.retrieveById(payment.id())).thenReturn(payment);

        // When
        PaymentDetailsResponse response = paymentController.getPayment(payment.id());

        // Then
        verify(paymentService).retrieveById(payment.id());
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(payment.id());
        assertThat(response.amount()).isEqualByComparingTo(payment.money().amount());
        assertThat(response.currency()).isEqualTo(payment.money().currency());
        assertThat(response.paymentMethod()).isEqualTo(payment.paymentMethod());
        assertThat(response.merchant()).isEqualTo(payment.merchant());
        assertThat(response.paymentStatus()).isEqualTo(payment.paymentStatus());
        assertThat(response.verifications()).hasSize(1);
        PaymentDetailsResponse.Verification verification = response.verifications().get(0);
        assertThat(verification.verificationType()).isEqualTo(paymentVerification.verificationType());
        assertThat(verification.verificationStatus()).isEqualTo(paymentVerification.verificationStatus());
    }

    @Test
    @DisplayName("Should not initialize payment when unexpected error occurs")
    public void testInitializePayment_ExceptionThrown() {
        // Given
        InitializePaymentRequest request = InitializePaymentRequest.builder()
                .amount(new BigDecimal("150.00"))
                .currency(Currency.EUR)
                .paymentMethod(PaymentMethod.PAY_NOW)
                .merchant("test-merchant")
                .build();
        when(paymentService.initializePayment(any())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThatThrownBy(() -> paymentController.initializePayment(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Service error");
        verify(paymentService).initializePayment(any());
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    public void testGetPayment_ExceptionThrown() {
        // Given
        String paymentId = "nonexistent";
        when(paymentService.retrieveById(paymentId)).thenThrow(new PaymentNotFoundException(paymentId));

        // When & Then
        assertThatThrownBy(() -> paymentController.getPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class);
        verify(paymentService).retrieveById(paymentId);
    }
}
