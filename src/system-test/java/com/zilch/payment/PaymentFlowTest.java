package com.zilch.payment;

import com.zilch.payment.adapter.inboud.api.dto.InitializePaymentRequest;
import com.zilch.payment.adapter.inboud.api.dto.PaymentDetailsResponse;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
public class PaymentFlowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    //I cannot implement any scenario because of random behaviour inside verifiers, so just a test checking that in overall payment is processed
    @Test
    @DisplayName("Full payment flow E2E test")
    public void testFullPaymentFlow() {
        // Create and configure the payment initialization request.
        InitializePaymentRequest initRequest = InitializePaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency(Currency.USD)
                .paymentMethod(PaymentMethod.PAY_NOW)
                .merchant("ExampleMerchant")
                .build();

        // Send POST request to initialize the payment.
        ResponseEntity<PaymentDetailsResponse> initResponse =
                restTemplate.postForEntity("/api/payments", initRequest, PaymentDetailsResponse.class);
        assertThat(initResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        PaymentDetailsResponse paymentResponse = initResponse.getBody();
        assertThat(paymentResponse).isNotNull();
        final String paymentId = paymentResponse.id();
        assertThat(paymentId).isNotBlank();

        //Wait for payment to be processed
        waitForVerificationToAppear(paymentId);
        waitForAllVerificationsToComplete(paymentId);
        checkPaymentStatusIsUpdated(paymentId);
    }

    private void waitForVerificationToAppear(String paymentId) {
        await().atMost(3, TimeUnit.SECONDS).until(() -> {
            PaymentDetailsResponse retrievedResponse =
                    restTemplate.getForEntity("/api/payments/" + paymentId, PaymentDetailsResponse.class).getBody();
            assertThat(retrievedResponse.paymentStatus()).isEqualTo(PaymentStatus.VERIFYING);
            return retrievedResponse.verifications() != null && !retrievedResponse.verifications().isEmpty();
        });
    }

    private void waitForAllVerificationsToComplete(String paymentId) {
        await().atMost(3, TimeUnit.SECONDS).until(() -> {
            PaymentDetailsResponse retrievedResponse =
                    restTemplate.getForEntity("/api/payments/" + paymentId, PaymentDetailsResponse.class).getBody();
            return Objects.requireNonNull(retrievedResponse).verifications()
                    .stream()
                    .map(PaymentDetailsResponse.Verification::verificationStatus)
                    .noneMatch(status -> status == PaymentVerificationStatus.SCHEDULED);
        });
    }

    private void checkPaymentStatusIsUpdated(String paymentId) {
        PaymentDetailsResponse payment =
                restTemplate.getForEntity("/api/payments/" + paymentId, PaymentDetailsResponse.class).getBody();
        assertThat(payment.paymentStatus()).isIn(PaymentStatus.ACCEPTED, PaymentStatus.REJECTED);
    }
}
