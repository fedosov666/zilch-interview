package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.domain.money.Money;
import com.zilch.payment.domain.payment.Payment;
import com.zilch.payment.domain.payment.enums.Currency;
import com.zilch.payment.domain.payment.enums.PaymentMethod;
import com.zilch.payment.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String merchant;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "paymentId", fetch = FetchType.EAGER)
    private List<PaymentVerificationEntity> paymentVerifications = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant lastUpdatedDate;

    @PrePersist
    void prePersist() {
        createdDate = Instant.now();
        lastUpdatedDate = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        lastUpdatedDate = Instant.now();
    }

    public Payment toPayment() {
        return Payment.builder()
                .id(id)
                .money(Money.builder().currency(currency).amount(amount).build())
                .paymentMethod(paymentMethod)
                .merchant(merchant)
                .paymentStatus(paymentStatus)
                .paymentVerifications(paymentVerifications.stream().map(PaymentVerificationEntity::toPaymentVerification).toList())
                .build();
    }

    public static PaymentEntity fromPayment(Payment payment) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(payment.id());
        paymentEntity.setAmount(payment.money().amount());
        paymentEntity.setCurrency(payment.money().currency());
        paymentEntity.setPaymentMethod(payment.paymentMethod());
        paymentEntity.setMerchant(payment.merchant());
        paymentEntity.setPaymentStatus(payment.paymentStatus());
        return paymentEntity;
    }
}
