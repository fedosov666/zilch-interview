package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.PaymentVerificationProvider;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DbPaymentVerificationProvider implements PaymentVerificationProvider {

    private final PaymentVerificationRepository paymentVerificationRepository;

    @Override
    public PaymentVerification save(PaymentVerification paymentVerification) {
        return paymentVerificationRepository.save(PaymentVerificationEntity.fromPaymentVerification(paymentVerification))
                .toPaymentVerification();
    }

    @Override
    @Transactional
    public void updateStatus(Long id, PaymentVerificationStatus paymentVerificationStatus) {
        paymentVerificationRepository.updatePaymentVerificationStatus(id, paymentVerificationStatus);
    }

}
