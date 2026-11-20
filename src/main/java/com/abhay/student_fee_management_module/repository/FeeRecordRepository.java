package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.FeeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {
}
