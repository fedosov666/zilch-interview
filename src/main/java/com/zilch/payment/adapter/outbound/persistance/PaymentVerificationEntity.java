package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.domain.verification.PaymentVerification;
import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import com.zilch.payment.domain.verification.enums.VerificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "payments_verifications")
@Getter
@Setter
@NoArgsConstructor
public class PaymentVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    private PaymentVerificationStatus verificationStatus;

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

    public PaymentVerification toPaymentVerification() {
        return PaymentVerification.builder()
                .id(id)
                .paymentId(paymentId)
                .verificationType(verificationType)
                .verificationStatus(verificationStatus)
                .build();
    }

    public static PaymentVerificationEntity fromPaymentVerification(PaymentVerification paymentVerification) {
        PaymentVerificationEntity entity = new PaymentVerificationEntity();
        entity.setPaymentId(paymentVerification.paymentId());
        entity.setVerificationType(paymentVerification.verificationType());
        entity.setVerificationStatus(paymentVerification.verificationStatus());
        return entity;
    }

}
