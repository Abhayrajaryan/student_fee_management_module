package com.abhay.student_fee_management_module.exception;

public class PaymentExceedsOutstandingException extends RuntimeException {

    public PaymentExceedsOutstandingException(String message) {
        super(message);
    }
}
