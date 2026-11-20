package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.response.PagedResponse;
import com.abhay.student_fee_management_module.dto.response.PendingFeeResponse;
import com.abhay.student_fee_management_module.dto.response.PendingStudentResponse;
import com.abhay.student_fee_management_module.entity.School;
import com.abhay.student_fee_management_module.entity.Student;
import com.abhay.student_fee_management_module.projections.FeeProjection;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import com.abhay.student_fee_management_module.repository.SchoolRepository;
import com.abhay.student_fee_management_module.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingStudentServiceTest {

    @Mock
    private FeeRecordRepository feeRecordRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SchoolRepository schoolRepository;

    private FeeRecordService feeRecordService;

    @BeforeEach
    void setUp() {
        feeRecordService = new FeeRecordService(
                feeRecordRepository, studentRepository, paymentRepository, schoolRepository);
    }

    @Test
    void mapsPendingFeesForMultipleStudentsAndPassesFilterAndPagination() {
        Student firstStudent =
                pendingStudent(17L, "Aarav Sharma", 3L, "Northside School");
        Student secondStudent =
                pendingStudent(18L, "Diya Sharma", 3L, "Northside School");
        PageRequest pageable = PageRequest.of(1, 2);
        when(studentRepository.findStudentsWithUnpaidFees(3L, pageable))
                .thenReturn(new PageImpl<>(List.of(firstStudent, secondStudent), pageable, 4));

        FeeProjection tuition = pendingFee(
                101L, "Tuition Fee", "15000", "5000", "10000", "2026-11-30");
        FeeProjection transport = pendingFee(
                102L, "Transport Fee", "3000", "1000", "2000", "2026-11-25");
        FeeProjection noPayments = pendingFee(
                201L, "Activity Fee", "500", "0", "500", "2026-12-01");
        when(feeRecordRepository.findPendingFeesByStudentId(17L))
                .thenReturn(List.of(tuition, transport));
        when(feeRecordRepository.findPendingFeesByStudentId(18L))
                .thenReturn(List.of(noPayments));

        PagedResponse<PendingStudentResponse> result =
                feeRecordService.getPendingStudents(3L, 1, 2);

        verify(studentRepository).findStudentsWithUnpaidFees(3L, pageable);
        verify(feeRecordRepository).findPendingFeesByStudentId(17L);
        verify(feeRecordRepository).findPendingFeesByStudentId(18L);
        assertEquals(2, result.getContent().size());

        PendingStudentResponse response = result.getContent().get(0);
        assertEquals(17L, response.getStudentId());
        assertEquals("Aarav Sharma", response.getStudentName());
        assertEquals(3L, response.getSchoolId());
        assertEquals("Northside School", response.getSchoolName());
        assertEquals(2, response.getPendingFees().size());
        assertPendingFee(response.getPendingFees().get(0), 101L, "Tuition Fee",
                "15000", "5000", "10000", LocalDate.of(2026, 11, 30));
        assertPendingFee(response.getPendingFees().get(1), 102L, "Transport Fee",
                "3000", "1000", "2000", LocalDate.of(2026, 11, 25));

        PendingStudentResponse secondResponse = result.getContent().get(1);
        assertEquals(18L, secondResponse.getStudentId());
        assertEquals(1, secondResponse.getPendingFees().size());
        assertPendingFee(secondResponse.getPendingFees().get(0), 201L, "Activity Fee",
                "500", "0", "500", LocalDate.of(2026, 12, 1));
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(4L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void passesNullSchoolFilterAndReturnsEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(studentRepository.findStudentsWithUnpaidFees(null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PagedResponse<PendingStudentResponse> result =
                feeRecordService.getPendingStudents(null, 0, 10);

        verify(studentRepository).findStudentsWithUnpaidFees(null, pageable);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.isLast());
    }

    @Test
    void skipsFeeAndPaymentQueriesWhenStudentPageIsEmpty() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(studentRepository.findStudentsWithUnpaidFees(null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        feeRecordService.getPendingStudents(null, 0, 10);

        verify(studentRepository).findStudentsWithUnpaidFees(null, pageable);
        org.mockito.Mockito.verifyNoInteractions(feeRecordRepository, paymentRepository);
    }

    private FeeProjection pendingFee(
            Long feeId,
            String title,
            String totalAmount,
            String paidAmount,
            String outstandingAmount,
            String dueDate) {
        FeeProjection projection = mock(FeeProjection.class);
        when(projection.getFeeId()).thenReturn(feeId);
        when(projection.getFeeTitle()).thenReturn(title);
        when(projection.getTotalAmount()).thenReturn(new BigDecimal(totalAmount));
        when(projection.getPaidAmount()).thenReturn(new BigDecimal(paidAmount));
        when(projection.getOutstandingAmount()).thenReturn(new BigDecimal(outstandingAmount));
        when(projection.getDueDate()).thenReturn(LocalDate.parse(dueDate));
        return projection;
    }

    private void assertPendingFee(
            PendingFeeResponse fee,
            Long feeId,
            String title,
            String totalAmount,
            String paidAmount,
            String outstandingAmount,
            LocalDate dueDate) {
        assertEquals(feeId, fee.getFeeId());
        assertEquals(title, fee.getFeeTitle());
        assertEquals(new BigDecimal(totalAmount), fee.getTotalAmount());
        assertEquals(new BigDecimal(paidAmount), fee.getPaidAmount());
        assertEquals(new BigDecimal(outstandingAmount), fee.getOutstandingAmount());
        assertEquals(dueDate, fee.getDueDate());
    }

    private Student pendingStudent(
            Long studentId, String studentName, Long schoolId, String schoolName) {
        School school = new School();
        school.setId(schoolId);
        school.setName(schoolName);

        Student student = new Student();
        student.setId(studentId);
        student.setName(studentName);
        student.setSchool(school);
        return student;
    }
}
