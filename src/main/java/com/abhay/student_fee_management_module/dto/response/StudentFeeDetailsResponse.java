package com.abhay.student_fee_management_module.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class StudentFeeDetailsResponse {

    private final Long feeId;
    private final String title;
    private final BigDecimal totalAmount;
    private final LocalDate dueDate;
    private final boolean fullyPaid;
    private final BigDecimal totalAmountPaid;
    private final BigDecimal outstandingAmount;
}
