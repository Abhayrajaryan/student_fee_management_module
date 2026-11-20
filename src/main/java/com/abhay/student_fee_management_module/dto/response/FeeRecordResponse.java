package com.abhay.student_fee_management_module.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FeeRecordResponse {

    private final Long id;
    private final Long studentId;
    private final String title;
    private final BigDecimal totalAmount;
    private final LocalDate dueDate;
    private final boolean fullyPaid;

}
