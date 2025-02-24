package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.PaymentFactory;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("h2")
class DbPaymentProviderIntegrationTests {

    @Autowired
    private DbPaymentProvider dbPaymentProvider;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Test save payment")
    void testSave() {
        //given
        Payment payment = PaymentFactory.createPayment(builder -> builder.id(null));

        //when
        Payment result = dbPaymentProvider.save(payment);

        //then
        assertThat(result.id()).isNotNull();
        assertThat(result.money().amount()).isEqualByComparingTo(payment.money().amount());
        assertThat(result.money().currency()).isEqualTo(payment.money().currency());
        assertThat(result.paymentMethod()).isEqualTo(payment.paymentMethod());
        assertThat(result.merchant()).isEqualTo(payment.merchant());
        assertThat(result.paymentStatus()).isEqualTo(payment.paymentStatus());
    }

    @Test
    @DisplayName("Test get payment by id")
    void testGetById() {
        //given
        Payment payment = PaymentFactory.createPayment(builder -> builder.id(null));
        PaymentEntity paymentEntity = PaymentEntity.fromPayment(payment);
        String paymentId = paymentRepository.save(paymentEntity).getId();

        //when
        Optional<Payment> result = dbPaymentProvider.getById(paymentId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(paymentId);
        assertThat(result.get().money().amount()).isEqualByComparingTo(payment.money().amount());
        assertThat(result.get().money().currency()).isEqualTo(payment.money().currency());
        assertThat(result.get().paymentMethod()).isEqualTo(payment.paymentMethod());
        assertThat(result.get().merchant()).isEqualTo(payment.merchant());
        assertThat(result.get().paymentStatus()).isEqualTo(payment.paymentStatus());
    }

    @Test
    @DisplayName("Test update payment status")
    void testSetPaymentStatus() {
        //given
        Payment payment = PaymentFactory.createPayment(builder -> builder.id(null));
        PaymentEntity paymentEntity = PaymentEntity.fromPayment(payment);
        String paymentId = paymentRepository.save(paymentEntity).getId();

        //when
        dbPaymentProvider.setPaymentStatus(paymentId, PaymentStatus.ACCEPTED);

        //then
        Optional<PaymentEntity> updatedEntity = paymentRepository.findById(paymentId);
        assertThat(updatedEntity).isPresent();
        assertThat(updatedEntity.get().getPaymentStatus()).isEqualTo(PaymentStatus.ACCEPTED);
    }
}
