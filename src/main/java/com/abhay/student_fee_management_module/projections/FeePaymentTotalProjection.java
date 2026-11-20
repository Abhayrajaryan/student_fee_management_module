package com.abhay.student_fee_management_module.projections;

import java.math.BigDecimal;

public interface FeePaymentTotalProjection {
    Long getFeeId();
    BigDecimal getTotalPaid();
}