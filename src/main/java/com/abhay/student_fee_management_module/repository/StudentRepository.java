package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
