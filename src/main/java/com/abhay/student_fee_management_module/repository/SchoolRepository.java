package com.abhay.student_fee_management_module.repository;

import com.abhay.student_fee_management_module.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
