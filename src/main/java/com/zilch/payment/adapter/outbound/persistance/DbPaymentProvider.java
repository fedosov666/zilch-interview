package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.PaymentProvider;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class DbPaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(PaymentEntity.fromPayment(payment)).toPayment();
    }

    @Override
    public Optional<Payment> getById(String id) {
        return paymentRepository.findById(id).map(PaymentEntity::toPayment);
    }

    @Override
    public void setPaymentStatus(String paymentId, PaymentStatus paymentStatus) {
        paymentRepository.updatePaymentStatus(paymentId, paymentStatus);
    }
}
