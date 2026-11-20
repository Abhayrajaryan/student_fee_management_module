package com.abhay.student_fee_management_module.dto.response;

import com.abhay.student_fee_management_module.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private final Long id;
    private final Long feeRecordId;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime paidAt;
}
