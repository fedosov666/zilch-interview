package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.domain.verification.enums.PaymentVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentVerificationRepository extends JpaRepository<PaymentVerificationEntity, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE PaymentVerificationEntity p SET p.verificationStatus = :status WHERE p.id = :id")
    void updatePaymentVerificationStatus(@Param("id") Long id, @Param("status") PaymentVerificationStatus paymentVerificationStatus);
}
