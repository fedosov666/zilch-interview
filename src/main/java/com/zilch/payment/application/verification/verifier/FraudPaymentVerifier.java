package com.zilch.payment.application.verification.verifier;

import com.zilch.payment.domain.money.CurrencyConversion;
import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@AllArgsConstructor
public class FraudPaymentVerifier implements PaymentVerifier {

    private static final Logger logger = LoggerFactory.getLogger(FraudPaymentVerifier.class);

    private final static Money CURRENCY_THRESHOLD = Money.builder()
            .currency(Currency.EUR)
            .amount(BigDecimal.TEN)
            .build();

    private final CurrencyConversion currencyConversion;

    @Override
    public VerificationType type() {
        return VerificationType.FRAUD_CHECK;
    }

    @Override
    public boolean shouldVerify(Payment payment) {
        if (payment.paymentMethod().isPayLaterMethod) {
            Money paymentInEur = currencyConversion.convert(payment.money(), Currency.EUR);
            return paymentInEur.isGreaterThan(CURRENCY_THRESHOLD);
        } else {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public PaymentVerificationStatus verify(Payment payment) {
        logger.info("Run fraud payment verification for {} payment", payment.id());
        //Work simulation
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 2001));
        return new Random().nextBoolean()
                ? PaymentVerificationStatus.PASSED
                : PaymentVerificationStatus.FAILED;
    }

}
