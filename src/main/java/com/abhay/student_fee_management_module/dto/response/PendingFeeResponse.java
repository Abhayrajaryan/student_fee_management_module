package com.abhay.student_fee_management_module.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PendingFeeResponse {
    private final Long feeId;
    private final String feeTitle;
    private final BigDecimal totalAmount;
    private final BigDecimal paidAmount;
    private final BigDecimal outstandingAmount;
    private final LocalDate dueDate;
}
