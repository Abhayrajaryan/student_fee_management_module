package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
            SELECT s
            FROM Student s
            WHERE (:schoolId IS NULL OR s.school.id = :schoolId)
              AND EXISTS (
                  SELECT fee.id
                  FROM FeeRecord fee
                  WHERE fee.student = s
                    AND fee.fullyPaid = false
              )
            ORDER BY s.id
            """)
    Page<Student> findStudentsWithUnpaidFees(@Param("schoolId") Long schoolId, Pageable pageable);
}
