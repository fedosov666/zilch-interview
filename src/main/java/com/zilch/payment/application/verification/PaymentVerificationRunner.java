package com.zilch.payment.application.verification;

import com.zilch.payment.application.verification.verifier.PaymentVerifier;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import com.zilch.payment.domain.verification.events.PaymentVerificationCompletedEvent;
import com.zilch.payment.domain.verification.events.ReadyForVerificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentVerificationRunner {

    private final Map<VerificationType, PaymentVerifier> verifiers;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public PaymentVerificationRunner(List<PaymentVerifier> verifiersList,
                                     ApplicationEventPublisher eventPublisher) {
        this.verifiers = verifiersList.stream().collect(Collectors.toMap(PaymentVerifier::type, verifier -> verifier));
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Async
    public void runVerification(ReadyForVerificationEvent event) {
        PaymentVerification paymentVerification = event.paymentVerification();
        Payment payment = event.payment();
        PaymentVerificationStatus paymentVerificationStatus = PaymentVerificationStatus.ERROR;
        try {
            paymentVerificationStatus = Optional.ofNullable(verifiers.get(paymentVerification.verificationType()))
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find verifier for: " + paymentVerification.verificationType()))
                    .verify(payment);
        } finally {
            eventPublisher.publishEvent(
                    PaymentVerificationCompletedEvent.builder()
                            .verificationId(paymentVerification.id())
                            .paymentId(payment.id())
                            .verificationResult(paymentVerificationStatus)
                            .build()
            );
        }
    }
}
