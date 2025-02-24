package com.zilch.payment.adapter.inboud.api;

import com.zilch.payment.adapter.inboud.api.dto.InitializePaymentRequest;
import com.zilch.payment.adapter.inboud.api.dto.PaymentDetailsResponse;
import com.zilch.payment.adapter.outbound.persistance.PaymentRepository;
import com.zilch.payment.adapter.outbound.persistance.PaymentVerificationRepository;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentControllerIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentVerificationRepository verificationRepository;

    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
    private static final Currency CURRENCY = Currency.USD;
    private static final String MERCHANT = "Test Merchant";
    private static final PaymentMethod PAYMENT_METHOD = PaymentMethod.PAY_NOW;

    @BeforeEach
    void setUp() {
        verificationRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create and retrieve payment")
    void shouldCreateAndRetrievePayment() {
        // Given
        InitializePaymentRequest request = new InitializePaymentRequest(
                AMOUNT, CURRENCY, PAYMENT_METHOD, MERCHANT
        );

        // When - Create payment
        ResponseEntity<PaymentDetailsResponse> createResponse = restTemplate.postForEntity(
                "/api/payments",
                request,
                PaymentDetailsResponse.class
        );

        // Then - Verify creation
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PaymentDetailsResponse paymentResponse = createResponse.getBody();
        assertThat(paymentResponse).isNotNull();
        String paymentId = paymentResponse.id();

        // Wait for payment verifications to appear
        await().atMost(2, TimeUnit.SECONDS).until(() ->
                !paymentRepository.findById(paymentId).orElseThrow().toPayment().paymentVerifications().isEmpty()
        );

        // When - Retrieve payment
        ResponseEntity<PaymentDetailsResponse> getResponse = restTemplate.getForEntity(
                "/api/payments/" + paymentId,
                PaymentDetailsResponse.class
        );

        // Then - Verify retrieval
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().id()).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("Should handle payment verification flow")
    void shouldHandlePaymentVerificationFlow() {
        // Given - Create a payment
        InitializePaymentRequest request = new InitializePaymentRequest(
                AMOUNT, CURRENCY, PAYMENT_METHOD, MERCHANT
        );
        ResponseEntity<PaymentDetailsResponse> createResponse = restTemplate.postForEntity(
                "/api/payments",
                request,
                PaymentDetailsResponse.class
        );
        String paymentId = createResponse.getBody().id();

        // When - Wait for verifications to complete
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    Payment payment = paymentRepository.findById(paymentId).orElseThrow().toPayment();
                    return payment.paymentVerifications().stream()
                            .allMatch(v -> v.verificationStatus() != PaymentVerificationStatus.SCHEDULED);
                });

        // Then - Verify final state
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<PaymentDetailsResponse> finalResponse = restTemplate.getForEntity(
                            "/api/payments/" + paymentId,
                            PaymentDetailsResponse.class
                    );
                    PaymentDetailsResponse finalPayment = finalResponse.getBody();
                    return List.of(PaymentStatus.ACCEPTED, PaymentStatus.REJECTED).contains(finalPayment.paymentStatus());
                });
    }

    @Test
    @DisplayName("Should handle concurrent payment requests")
    void shouldHandleConcurrentPaymentRequests() throws Exception {
        int numberOfRequests = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);

        List<Future<ResponseEntity<PaymentDetailsResponse>>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            futures.add(executor.submit(() -> {
                try {
                    InitializePaymentRequest request = new InitializePaymentRequest(
                            AMOUNT, CURRENCY, PAYMENT_METHOD, MERCHANT
                    );
                    return restTemplate.postForEntity(
                            "/api/payments",
                            request,
                            PaymentDetailsResponse.class
                    );
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(10, TimeUnit.SECONDS);

        // Verify all payments were created successfully
        for (Future<ResponseEntity<PaymentDetailsResponse>> future : futures) {
            ResponseEntity<PaymentDetailsResponse> response = future.get();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        // Verify database state
        long paymentCount = paymentRepository.count();
        assertThat(paymentCount).isEqualTo(numberOfRequests);
    }

    @ParameterizedTest
    @DisplayName("Should not initialize payment with invalid input")
    @MethodSource("provideInvalidPaymentRequests")
    void testInitializePayment_InvalidInput(InitializePaymentRequest request, String expectedErrorMessage) {
        // When & Then
        ResponseEntity<String> response = restTemplate.postForEntity("/api/payments", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains(expectedErrorMessage);
    }

    private static Stream<Arguments> provideInvalidPaymentRequests() {
        return Stream.of(
                Arguments.of(
                        InitializePaymentRequest.builder()
                                .amount(new BigDecimal("0.00"))
                                .currency(Currency.EUR)
                                .paymentMethod(PaymentMethod.PAY_NOW)
                                .merchant("test-merchant")
                                .build(),
                        "Amount must be higher than 0"
                ),
                Arguments.of(
                        InitializePaymentRequest.builder()
                                .amount(new BigDecimal("150.00"))
                                .currency(null) // Invalid currency
                                .paymentMethod(PaymentMethod.PAY_NOW)
                                .merchant("test-merchant")
                                .build(),
                        "Currency is required"
                ),
                Arguments.of(
                        InitializePaymentRequest.builder()
                                .amount(new BigDecimal("150.00"))
                                .currency(Currency.EUR)
                                .paymentMethod(null) // Invalid payment method
                                .merchant("test-merchant")
                                .build(),
                        "Payment method is required"
                ),
                Arguments.of(
                        InitializePaymentRequest.builder()
                                .amount(new BigDecimal("150.00"))
                                .currency(Currency.EUR)
                                .paymentMethod(PaymentMethod.PAY_NOW)
                                .merchant(null) // Merchant should not be null
                                .build(),
                        "Merchant should be provided"
                ),
                Arguments.of(
                        InitializePaymentRequest.builder()
                                .amount(new BigDecimal("150.00"))
                                .currency(Currency.EUR)
                                .paymentMethod(PaymentMethod.PAY_NOW)
                                .merchant("") // Empty merchant
                                .build(),
                        "Merchant should be provided"
                )
        );
    }
}
