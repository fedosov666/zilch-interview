package com.zilch.payment.adapter.outbound.persistance;

import com.zilch.payment.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    @Modifying
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.paymentStatus = :paymentStatus WHERE p.id = :paymentId")
    void updatePaymentStatus(@Param("paymentId") String paymentId, @Param("paymentStatus") PaymentStatus paymentStatus);
}
