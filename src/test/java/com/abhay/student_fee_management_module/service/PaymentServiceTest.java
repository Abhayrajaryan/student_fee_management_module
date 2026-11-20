package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreatePaymentRequest;
import com.abhay.student_fee_management_module.dto.response.PaymentResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Payment;
import com.abhay.student_fee_management_module.enums.PaymentMethod;
import com.abhay.student_fee_management_module.exception.PaymentExceedsOutstandingException;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FeeRecordRepository feeRecordRepository;

    private PaymentService paymentService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, feeRecordRepository);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void successfulFullPayment() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(new BigDecimal("25000.00"), PaymentMethod.UPI);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(101L);
            return payment;
        });

        PaymentResponse response = paymentService.recordPayment(request);

        assertEquals(101L, response.getId());
        assertEquals(1L, response.getFeeRecordId());
        assertEquals(new BigDecimal("25000.00"), response.getAmount());
        assertEquals(PaymentMethod.UPI, response.getPaymentMethod());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void successfulPartialPayment() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(new BigDecimal("10000.00"), PaymentMethod.CASH);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(102L);
            return payment;
        });

        PaymentResponse response = paymentService.recordPayment(request);

        assertEquals(new BigDecimal("10000.00"), response.getAmount());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void multiplePartialPaymentsCumulativelySettleFeeRecord() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("30000.00"));
        CreatePaymentRequest firstRequest = request(new BigDecimal("10000.00"), PaymentMethod.UPI);
        CreatePaymentRequest secondRequest = request(new BigDecimal("20000.00"), PaymentMethod.CARD);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L))
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(new BigDecimal("10000.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(103L);
            return payment;
        });

        paymentService.recordPayment(firstRequest);
        PaymentResponse settledResponse = paymentService.recordPayment(secondRequest);

        assertEquals(new BigDecimal("20000.00"), settledResponse.getAmount());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void feeRecordNotFound() {
        when(feeRecordRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.recordPayment(
                        request(99L, new BigDecimal("100.00"), PaymentMethod.CASH)));

        assertEquals("Fee record not found with ID: 99", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void paymentAmountGreaterThanOutstanding() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(new BigDecimal("20000.00"), PaymentMethod.CASH);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L))
                .thenReturn(new BigDecimal("10000.00"));

        assertThrows(PaymentExceedsOutstandingException.class,
                () -> paymentService.recordPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void paymentAmountEqualToOutstanding() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(new BigDecimal("15000.00"), PaymentMethod.BANK_TRANSFER);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L))
                .thenReturn(new BigDecimal("10000.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(104L);
            return payment;
        });

        PaymentResponse response = paymentService.recordPayment(request);

        assertEquals(new BigDecimal("15000.00"), response.getAmount());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void noPreviousPayments() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("5000.00"));
        CreatePaymentRequest request = request(new BigDecimal("5000.00"), PaymentMethod.CHEQUE);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(105L);
            return payment;
        });

        paymentService.recordPayment(request);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(feeRecord, captor.getValue().getFeeRecord());
    }

    @Test
    void invalidRequestFailsBeanValidation() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(BigDecimal.ZERO);

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("feeId")));
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("amount")));
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("paymentMethod")));
        verifyNoInteractions(feeRecordRepository, paymentRepository);
    }

    private FeeRecord feeRecord(Long id, BigDecimal totalAmount) {
        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setId(id);
        feeRecord.setTotalAmount(totalAmount);
        return feeRecord;
    }

    private CreatePaymentRequest request(BigDecimal amount, PaymentMethod paymentMethod) {
        return request(1L, amount, paymentMethod);
    }

    private CreatePaymentRequest request(Long feeId, BigDecimal amount,
                                         PaymentMethod paymentMethod) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setFeeId(feeId);
        request.setAmount(amount);
        request.setPaymentMethod(paymentMethod);
        return request;
    }
}
