package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreateFeeRecordRequest;
import com.abhay.student_fee_management_module.dto.response.FeeRecordResponse;
import com.abhay.student_fee_management_module.dto.response.PagedResponse;
import com.abhay.student_fee_management_module.dto.response.StudentFeeDetailsResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Student;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.projections.FeePaymentTotalProjection;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import com.abhay.student_fee_management_module.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeRecordServiceTest {

    @Mock
    private FeeRecordRepository feeRecordRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private FeeRecordService feeRecordService;

    @BeforeEach
    void setUp() {
        feeRecordService = new FeeRecordService(
                feeRecordRepository, studentRepository, paymentRepository);
    }

    @Test
    void createFeeRecordSuccessfully() {
        Student student = new Student();
        student.setId(15L);
        CreateFeeRecordRequest request = validRequest();
        request.setTitle("  Term 1 Tuition Fee  ");

        when(studentRepository.findById(15L)).thenReturn(Optional.of(student));
        when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> {
            FeeRecord feeRecord = invocation.getArgument(0);
            feeRecord.setId(101L);
            return feeRecord;
        });

        FeeRecordResponse response = feeRecordService.createFeeRecord(request);

        ArgumentCaptor<FeeRecord> captor = ArgumentCaptor.forClass(FeeRecord.class);
        verify(feeRecordRepository).save(captor.capture());
        FeeRecord saved = captor.getValue();
        assertSame(student, saved.getStudent());
        assertEquals("Term 1 Tuition Fee", saved.getTitle());
        assertEquals(new BigDecimal("25000.00"), saved.getTotalAmount());
        assertEquals(LocalDate.of(2026, 12, 15), saved.getDueDate());
        assertEquals(101L, response.getId());
        assertEquals(15L, response.getStudentId());
    }

    @Test
    void throwsResourceNotFoundWhenStudentDoesNotExist() {
        CreateFeeRecordRequest request = validRequest();
        when(studentRepository.findById(15L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feeRecordService.createFeeRecord(request));

        assertEquals("Student not found with ID: 15", exception.getMessage());
        verify(feeRecordRepository, never()).save(any(FeeRecord.class));
    }

    @Test
    void returnsEmptyListWhenStudentHasNoFeeRecords() {
        Student student = new Student();
        student.setId(15L);
        when(studentRepository.findById(15L)).thenReturn(Optional.of(student));
        when(feeRecordRepository.findByStudentId(15L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        PagedResponse<StudentFeeDetailsResponse> fees =
                feeRecordService.getStudentFees(15L, 0, 10);

        assertTrue(fees.getContent().isEmpty());
        assertEquals(0, fees.getTotalElements());
        assertEquals(0, fees.getTotalPages());
        assertTrue(fees.isLast());
        verify(paymentRepository, never()).sumAmountsByFeeRecordIds(any());
    }

    @Test
    void throwsResourceNotFoundWhenRetrievingFeesForMissingStudent() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feeRecordService.getStudentFees(99L, 0, 10));

        assertEquals("Student not found with ID: 99", exception.getMessage());
        verify(feeRecordRepository, never()).findByStudentId(any(), any());
    }

    @Test
    void calculatesPaidAndOutstandingAmountsForStudentFees() {
        Student student = new Student();
        student.setId(15L);
        FeeRecord firstFee = new FeeRecord();
        firstFee.setId(101L);
        firstFee.setTitle("Tuition Fee");
        firstFee.setTotalAmount(new BigDecimal("25000.00"));
        firstFee.setDueDate(LocalDate.of(2026, 12, 15));
        FeeRecord secondFee = new FeeRecord();
        secondFee.setId(102L);
        secondFee.setTitle("Transport Fee");
        secondFee.setTotalAmount(new BigDecimal("5000.00"));
        secondFee.setDueDate(LocalDate.of(2026, 12, 20));

        when(studentRepository.findById(15L)).thenReturn(Optional.of(student));
        when(feeRecordRepository.findByStudentId(15L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(
                        List.of(firstFee, secondFee),
                        PageRequest.of(0, 10),
                        2));

        FeePaymentTotalProjection projection = mock(FeePaymentTotalProjection.class);

        when(projection.getFeeId()).thenReturn(101L);
        when(projection.getTotalPaid()).thenReturn(new BigDecimal("10000.00"));

        List<FeePaymentTotalProjection> paymentTotals = List.of(projection);

        when(paymentRepository.sumAmountsByFeeRecordIds(List.of(101L, 102L)))
                .thenReturn(paymentTotals);

        PagedResponse<StudentFeeDetailsResponse> fees =
                feeRecordService.getStudentFees(15L, 0, 10);

        assertEquals(2, fees.getContent().size());
        assertEquals(new BigDecimal("10000.00"),
                fees.getContent().get(0).getTotalAmountPaid());
        assertEquals(new BigDecimal("15000.00"),
                fees.getContent().get(0).getOutstandingAmount());
        assertEquals(BigDecimal.ZERO,
                fees.getContent().get(1).getTotalAmountPaid());
        assertEquals(new BigDecimal("5000.00"),
                fees.getContent().get(1).getOutstandingAmount());
    }

    @Test
    void returnsPaginationMetadataForLastPage() {
        Student student = new Student();
        student.setId(15L);
        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setId(105L);
        feeRecord.setTitle("Exam Fee");
        feeRecord.setTotalAmount(new BigDecimal("3000.00"));
        feeRecord.setDueDate(LocalDate.of(2026, 12, 30));

        PageRequest pageable = PageRequest.of(2, 2);
        when(studentRepository.findById(15L)).thenReturn(Optional.of(student));
        when(feeRecordRepository.findByStudentId(15L, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(feeRecord), pageable, 5));

        PagedResponse<StudentFeeDetailsResponse> fees =
                feeRecordService.getStudentFees(15L, 2, 2);

        assertEquals(1, fees.getContent().size());
        assertEquals(2, fees.getPage());
        assertEquals(2, fees.getSize());
        assertEquals(5, fees.getTotalElements());
        assertEquals(3, fees.getTotalPages());
        assertTrue(fees.isLast());
    }

    private CreateFeeRecordRequest validRequest() {
        CreateFeeRecordRequest request = new CreateFeeRecordRequest();
        request.setStudentId(15L);
        request.setTitle("Term 1 Tuition Fee");
        request.setTotalAmount(new BigDecimal("25000.00"));
        request.setDueDate(LocalDate.of(2026, 12, 15));
        return request;
    }
}
