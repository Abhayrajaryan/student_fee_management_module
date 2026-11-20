package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreateFeeRecordRequest;
import com.abhay.student_fee_management_module.dto.response.FeeRecordResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Student;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private FeeRecordService feeRecordService;

    @BeforeEach
    void setUp() {
        feeRecordService = new FeeRecordService(feeRecordRepository, studentRepository);
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

    private CreateFeeRecordRequest validRequest() {
        CreateFeeRecordRequest request = new CreateFeeRecordRequest();
        request.setStudentId(15L);
        request.setTitle("Term 1 Tuition Fee");
        request.setTotalAmount(new BigDecimal("25000.00"));
        request.setDueDate(LocalDate.of(2026, 12, 15));
        return request;
    }
}
