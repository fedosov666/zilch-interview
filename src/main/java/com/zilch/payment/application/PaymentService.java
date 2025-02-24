package com.zilch.payment.application;

import com.zilch.payment.application.verification.PaymentVerificationScheduler;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentNotFoundException;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.FuturePaymentDetails;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentProvider paymentProvider;
    private final PaymentVerificationScheduler paymentVerificationScheduler;

    public Payment initializePayment(FuturePaymentDetails futurePaymentDetails) {
        logger.info("Create new payment: {}", futurePaymentDetails);
        Payment newPayment = paymentProvider.save(futurePaymentDetails.toNewPayment());
        paymentVerificationScheduler.scheduleVerifications(newPayment);
        return newPayment;
    }

    public Payment retrieveById(String paymentId) {
        logger.info("Retrieve payment details by id: {}", paymentId);
        return paymentProvider.getById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

}
