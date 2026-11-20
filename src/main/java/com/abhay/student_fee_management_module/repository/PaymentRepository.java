package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM Payment payment
            WHERE payment.feeRecord.id = :feeRecordId
            """)
    BigDecimal sumAmountByFeeRecordId(@Param("feeRecordId") Long feeRecordId);
}
