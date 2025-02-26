package com.zilch.payment.application.verification;

import com.zilch.payment.application.verification.verifier.FraudPaymentVerifier;
import com.zilch.payment.application.verification.verifier.PaymentVerifier;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentCreatedEvent;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.PaymentVerificationProvider;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.events.ReadyForVerificationEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentVerificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FraudPaymentVerifier.class);

    private final PaymentProvider paymentProvider;
    private final PaymentVerificationProvider paymentVerificationProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final List<PaymentVerifier> paymentVerifiers;

    @EventListener
    @Async("verificationSchedulerExecutor")
    public void scheduleVerifications(PaymentCreatedEvent paymentCreatedEvent) {
        Payment payment = paymentCreatedEvent.payment();
        try {
            trySchedule(payment);
        } catch (Exception e) {
            logger.error("Cannot schedule payment verifications, reject payment", e);
            paymentProvider.setPaymentStatus(payment.id(), PaymentStatus.REJECTED);
        }
    }

    private void trySchedule(Payment payment) {
        List<PaymentVerification> verifications = paymentVerifiers.stream()
                .filter(paymentVerifier -> paymentVerifier.shouldVerify(payment))
                .map(PaymentVerifier::type)
                .map(type -> PaymentVerification.builder()
                        .verificationStatus(PaymentVerificationStatus.SCHEDULED)
                        .verificationType(type)
                        .paymentId(payment.id())
                        .build()
                )
                .map(paymentVerificationProvider::save)
                .toList();
        if (verifications.isEmpty()) {
            logger.info("No verifications should be scheduled for payment: {}", payment.id());
            paymentProvider.setPaymentStatus(payment.id(), PaymentStatus.ACCEPTED);
        } else {
            verifications.forEach(verification -> {
                logger.info("Schedule the {} verification for the payment: {}", verification.verificationType(), verification.paymentId());
                eventPublisher.publishEvent(ReadyForVerificationEvent.builder().paymentVerification(verification).payment(payment).build());
            });
            paymentProvider.setPaymentStatus(payment.id(), PaymentStatus.VERIFYING);
        }
    }

}
