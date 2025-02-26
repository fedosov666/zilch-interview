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
public class AccountStatusVerifier implements PaymentVerifier {

    private static final Logger logger = LoggerFactory.getLogger(AccountStatusVerifier.class);

    @Override
    public VerificationType type() {
        return VerificationType.ACCOUNT_STATUS_CHECK;
    }

    @Override
    public boolean shouldVerify(Payment payment) {
        return true;
    }

    @SneakyThrows
    @Override
    public PaymentVerificationStatus verify(Payment payment) {
        logger.info("Run account status payment verification for {} payment", payment.id());
        //All accounts are fine, but in a real env we can make some api calls here to verify an account in another ms
        //Work simulation
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 1001));
        //An exception can simulate unavailability of some external api
        /*if (new Random().nextBoolean()) {
            throw new RuntimeException("Processing error");
        }*/
        return PaymentVerificationStatus.PASSED;
    }

}
