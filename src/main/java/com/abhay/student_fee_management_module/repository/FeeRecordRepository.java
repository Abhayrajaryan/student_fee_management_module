package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.projections.FeeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {
    Page<FeeRecord> findByStudentId(Long studentId, Pageable pageable);

    @Query("""
        SELECT fee.id AS feeId,
               fee.title AS feeTitle,
               fee.totalAmount AS totalAmount,
               COALESCE(SUM(payment.amount), 0) AS paidAmount,
               fee.totalAmount - COALESCE(SUM(payment.amount), 0) AS outstandingAmount,
               fee.dueDate AS dueDate
        FROM Payment payment
        JOIN payment.feeRecord fee
        WHERE fee.id IN :feeRecordIds
        GROUP BY fee.id, fee.title, fee.totalAmount, fee.dueDate
        """)
    List<FeeProjection> sumAmountsByFeeRecordIds(@Param("feeRecordIds") List<Long> feeRecordIds);

    @Query("""
            SELECT fee.id AS feeId,
                   fee.title AS feeTitle,
                   fee.totalAmount AS totalAmount,
                   COALESCE(SUM(payment.amount), 0) AS paidAmount,
                   fee.totalAmount - COALESCE(SUM(payment.amount), 0) AS outstandingAmount,
                   fee.dueDate AS dueDate
            FROM FeeRecord fee
            LEFT JOIN fee.payments payment
            WHERE fee.student.id = :studentId
              AND fee.fullyPaid = false
            GROUP BY fee.id, fee.title, fee.totalAmount, fee.dueDate
            """)
    List<FeeProjection> findPendingFeesByStudentId(@Param("studentId") Long studentId);
}
