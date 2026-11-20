package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreatePaymentRequest;
import com.abhay.student_fee_management_module.dto.response.PaymentResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Payment;
import com.abhay.student_fee_management_module.exception.PaymentExceedsOutstandingException;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeRecordRepository feeRecordRepository;

    @Transactional
    public PaymentResponse recordPayment(CreatePaymentRequest request) {
        Long feeId = request.getFeeId();
        FeeRecord feeRecord = feeRecordRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fee record not found with ID: " + feeId));

        BigDecimal totalPaid = paymentRepository.sumAmountByFeeRecordId(feeId);
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        BigDecimal outstandingAmount = feeRecord.getTotalAmount().subtract(totalPaid);
        if (request.getAmount().compareTo(outstandingAmount) > 0) {
            throw new PaymentExceedsOutstandingException(
                    "Payment amount exceeds the outstanding balance");
        }

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaidAt(LocalDateTime.now());
        payment.setFeeRecord(feeRecord);

        Payment savedPayment = paymentRepository.save(payment);
        return new PaymentResponse(
                savedPayment.getId(),
                feeRecord.getId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentMethod(),
                savedPayment.getPaidAt());
    }
}
