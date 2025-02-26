package com.zilch.payment.application.verification;

import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentNotFoundException;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import com.zilch.payment.domain.verification.PaymentVerificationProvider;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.events.PaymentVerificationCompletedEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentVerificationResultAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentVerificationResultAnalyzer.class);

    private final PaymentProvider paymentProvider;
    private final PaymentVerificationProvider paymentVerificationProvider;

    @EventListener
    @Async("verificationAnalyzerExecutor")
    public void processPaymentVerificationResult(PaymentVerificationCompletedEvent event) {
        logger.debug("Retrieved PaymentVerificationCompletedEvent event: " + event.toString());
        paymentVerificationProvider.updateStatus(event.verificationId(), event.verificationResult());
        Payment payment = paymentProvider.getById(event.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(event.paymentId()));
        if (event.verificationResult() == PaymentVerificationStatus.PASSED) {
            boolean allPassed = payment.paymentVerifications().stream()
                    .allMatch(paymentVerification -> paymentVerification.verificationStatus() == PaymentVerificationStatus.PASSED);
            if (allPassed) {
                logger.info("Accept {} payment", payment.id());
                paymentProvider.setPaymentStatus(payment.id(), PaymentStatus.ACCEPTED);
            }
        } else {
            logger.info("Reject {} payment", payment.id());
            paymentProvider.setPaymentStatus(payment.id(), PaymentStatus.REJECTED);
        }
    }

}
