package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.Payment;
import com.abhay.student_fee_management_module.projections.FeeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByFeeRecordId(Long feeRecordId);

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM Payment payment
            WHERE payment.feeRecord.id = :feeRecordId
            """)
    BigDecimal sumAmountByFeeRecordId(@Param("feeRecordId") Long feeRecordId);

    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0)
            FROM Payment payment
            WHERE payment.feeRecord.student.school.id = :schoolId
            """)
    BigDecimal sumCollectedBySchoolId(@Param("schoolId") Long schoolId);

}
