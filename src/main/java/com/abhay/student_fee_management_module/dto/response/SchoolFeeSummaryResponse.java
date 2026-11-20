package com.abhay.student_fee_management_module.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SchoolFeeSummaryResponse {
    private final Long schoolId;
    private final String schoolName;
    private final BigDecimal totalFees;
    private final BigDecimal totalCollected;
    private final BigDecimal totalOutstanding;
    private final Long totalFeeRecords;
    private final Long fullyPaidRecords;
    private final Long pendingRecords;
}
