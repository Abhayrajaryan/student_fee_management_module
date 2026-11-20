package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
