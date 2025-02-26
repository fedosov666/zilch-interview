package com.zilch.payment.application;

import com.zilch.payment.application.verification.PaymentVerificationScheduler;
import com.zilch.payment.domain.payment.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentProvider paymentProvider;
    private final ApplicationEventPublisher eventPublisher;

    public Payment initializePayment(FuturePaymentDetails futurePaymentDetails) {
        logger.info("Create new payment: {}", futurePaymentDetails);
        Payment newPayment = paymentProvider.save(futurePaymentDetails.toNewPayment());
        eventPublisher.publishEvent(new PaymentCreatedEvent((newPayment)));
        return newPayment;
    }

    public Payment retrieveById(String paymentId) {
        logger.info("Retrieve payment details by id: {}", paymentId);
        return paymentProvider.getById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

}
