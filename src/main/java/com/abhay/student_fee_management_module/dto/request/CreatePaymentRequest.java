package com.abhay.student_fee_management_module.dto.request;

import com.abhay.student_fee_management_module.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "Fee ID is required")
    @Positive(message = "Fee ID must be positive")
    private Long feeId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Paid at date and time is required")
    private LocalDateTime paidAt;
}
