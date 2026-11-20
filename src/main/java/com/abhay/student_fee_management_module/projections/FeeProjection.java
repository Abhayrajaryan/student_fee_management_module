package com.abhay.student_fee_management_module.projections;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FeeProjection {
    Long getFeeId();
    String getFeeTitle();
    BigDecimal getTotalAmount();
    BigDecimal getPaidAmount();
    BigDecimal getOutstandingAmount();
    LocalDate getDueDate();
}
