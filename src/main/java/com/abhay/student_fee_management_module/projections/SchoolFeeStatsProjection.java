package com.abhay.student_fee_management_module.projections;

import java.math.BigDecimal;

public interface SchoolFeeStatsProjection {
    BigDecimal getTotalFees();
    Long getTotalFeeRecords();
    Long getFullyPaidRecords();
    Long getPendingRecords();
}
