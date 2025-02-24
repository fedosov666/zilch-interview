package com.zilch.payment.application.verification.verifier;

import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class CreditLimitPaymentVerifier implements PaymentVerifier {

    private static final Logger logger = LoggerFactory.getLogger(CreditLimitPaymentVerifier.class);

    @Override
    public VerificationType type() {
        return VerificationType.CREDIT_LIMIT_CHECK;
    }

    @Override
    public boolean shouldVerify(Payment payment) {
        return payment.paymentMethod().isPayLaterMethod;
    }

    @SneakyThrows
    @Override
    public PaymentVerificationStatus verify(Payment payment) {
        logger.info("Run credit limit payment verification for {} payment", payment.id());
        //Work simulation
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 2001));
        return PaymentVerificationStatus.PASSED;
    }

}
