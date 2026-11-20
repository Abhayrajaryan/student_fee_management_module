package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.FeeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeeRecordRepository extends JpaRepository<FeeRecord, Long> {
    Page<FeeRecord> findByStudentId(Long studentId, Pageable pageable);
}
