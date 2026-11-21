package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreatePaymentRequest;
import com.abhay.student_fee_management_module.dto.response.PaymentHistoryResponse;
import com.abhay.student_fee_management_module.dto.response.PaymentResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Payment;
import com.abhay.student_fee_management_module.enums.PaymentMethod;
import com.abhay.student_fee_management_module.exception.PaymentExceedsOutstandingException;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final LocalDateTime PAID_AT =
            LocalDateTime.of(2026, 11, 20, 16, 30);

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FeeRecordRepository feeRecordRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, feeRecordRepository);
    }

    @Test
    void successfulFullPayment() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(
                1L, new BigDecimal("25000.00"), PaymentMethod.UPI);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(101L);
            return payment;
        });

        PaymentResponse response = paymentService.recordPayment(request);

        assertEquals(101L, response.getId());
        assertEquals(new BigDecimal("25000.00"), response.getAmount());
        assertEquals(PaymentMethod.UPI, response.getPaymentMethod());
        assertTrue(feeRecord.isFullyPaid());
        verify(paymentRepository).save(any(Payment.class));
        verify(feeRecordRepository).save(feeRecord);
    }

    @Test
    void successfulPartialPayment() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(
                1L, new BigDecimal("10000.00"), PaymentMethod.CASH);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(102L);
            return payment;
        });

        paymentService.recordPayment(request);

        assertFalse(feeRecord.isFullyPaid());
        verify(paymentRepository).save(any(Payment.class));
        verify(feeRecordRepository).save(feeRecord);
    }

    @Test
    void multiplePartialPaymentsCumulativelySettleFeeRecord() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("30000.00"));
        CreatePaymentRequest firstRequest = request(
                1L, new BigDecimal("10000.00"), PaymentMethod.UPI);
        CreatePaymentRequest secondRequest = request(
                1L, new BigDecimal("20000.00"), PaymentMethod.CARD);

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
        assertFalse(feeRecord.isFullyPaid());

        paymentService.recordPayment(secondRequest);

        assertTrue(feeRecord.isFullyPaid());
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(captor.capture());
        assertEquals(new BigDecimal("10000.00"), captor.getAllValues().get(0).getAmount());
        assertEquals(new BigDecimal("20000.00"), captor.getAllValues().get(1).getAmount());
        verify(feeRecordRepository, times(2)).save(feeRecord);
    }

    @Test
    void paymentAmountExceedsOutstandingAmount() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(
                1L, new BigDecimal("20000.00"), PaymentMethod.CASH);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L))
                .thenReturn(new BigDecimal("10000.00"));

        assertThrows(PaymentExceedsOutstandingException.class,
                () -> paymentService.recordPayment(request));

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(feeRecordRepository, never()).save(any(FeeRecord.class));
        assertFalse(feeRecord.isFullyPaid());
    }

    @Test
    void feeRecordDoesNotExist() {
        when(feeRecordRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.recordPayment(request(
                        99L, new BigDecimal("100.00"), PaymentMethod.CASH)));

        assertEquals("Fee record not found with ID: 99", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void paymentAmountEqualsOutstandingAmount() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(
                1L, new BigDecimal("15000.00"), PaymentMethod.BANK_TRANSFER);

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
        assertTrue(feeRecord.isFullyPaid());
        verify(paymentRepository).save(any(Payment.class));
        verify(feeRecordRepository).save(feeRecord);
    }

    @Test
    void noPreviousPayments() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("5000.00"));
        CreatePaymentRequest request = request(
                1L, new BigDecimal("5000.00"), PaymentMethod.CHEQUE);

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
        assertEquals(new BigDecimal("5000.00"), captor.getValue().getAmount());
        assertTrue(feeRecord.isFullyPaid());
    }

    @Test
    void paidAtIsAssignedToSavedPaymentEntity() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        CreatePaymentRequest request = request(
                1L, new BigDecimal("10000.00"), PaymentMethod.UPI);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.sumAmountByFeeRecordId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(106L);
            return payment;
        });

        paymentService.recordPayment(request);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(request.getPaidAt(), captor.getValue().getPaidAt());
        assertEquals(PAID_AT, captor.getValue().getPaidAt());
        assertEquals(feeRecord, captor.getValue().getFeeRecord());
        assertEquals(new BigDecimal("10000.00"), captor.getValue().getAmount());
        assertEquals(PaymentMethod.UPI, captor.getValue().getPaymentMethod());
    }

    @Test
    void retrievesPaymentHistoryForExistingFeeRecord() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        Payment payment = new Payment();
        payment.setId(101L);
        payment.setAmount(new BigDecimal("10000.00"));
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaidAt(PAID_AT);
        payment.setFeeRecord(feeRecord);

        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.findByFeeRecordId(1L)).thenReturn(List.of(payment));

        List<PaymentHistoryResponse> history = paymentService.getPaymentHistory(1L);

        assertEquals(1, history.size());
        assertEquals(101L, history.get(0).getPaymentId());
        assertEquals(1L, history.get(0).getFeeId());
        assertEquals(new BigDecimal("10000.00"), history.get(0).getAmount());
        assertEquals(PaymentMethod.UPI, history.get(0).getPaymentMethod());
        assertEquals(PAID_AT, history.get(0).getPaidAt());
        verify(paymentRepository).findByFeeRecordId(1L);
    }

    @Test
    void returnsEmptyHistoryWhenFeeRecordHasNoPayments() {
        FeeRecord feeRecord = feeRecord(1L, new BigDecimal("25000.00"));
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(feeRecord));
        when(paymentRepository.findByFeeRecordId(1L)).thenReturn(List.of());

        List<PaymentHistoryResponse> history = paymentService.getPaymentHistory(1L);

        assertTrue(history.isEmpty());
        verify(paymentRepository).findByFeeRecordId(1L);
    }

    @Test
    void throwsNotFoundWhenRetrievingHistoryForMissingFeeRecord() {
        when(feeRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentHistory(99L));

        verify(paymentRepository, never()).findByFeeRecordId(any());
    }

    private FeeRecord feeRecord(Long id, BigDecimal totalAmount) {
        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setId(id);
        feeRecord.setTotalAmount(totalAmount);
        return feeRecord;
    }

    private CreatePaymentRequest request(
            Long feeId, BigDecimal amount, PaymentMethod paymentMethod) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setFeeId(feeId);
        request.setAmount(amount);
        request.setPaymentMethod(paymentMethod);
        request.setPaidAt(PAID_AT);
        return request;
    }
}
