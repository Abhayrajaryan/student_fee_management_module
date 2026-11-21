package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.response.SchoolFeeSummaryResponse;
import com.abhay.student_fee_management_module.entity.School;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.projections.SchoolFeeStatsProjection;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import com.abhay.student_fee_management_module.repository.SchoolRepository;
import com.abhay.student_fee_management_module.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolFeeSummaryServiceTest {

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
    void returnsZeroTotalsWhenSchoolHasNoFeeRecords() {
        assertSummary("0", "0", "0", 0L, 0L, 0L);
    }

    @Test
    void returnsUnpaidFeeTotals() {
        assertSummary("1500", "0", "1500", 2L, 0L, 2L);
    }

    @Test
    void returnsPartialPaymentTotals() {
        assertSummary("1000", "300", "700", 1L, 0L, 1L);
    }

    @Test
    void returnsFullyPaidFeeTotals() {
        assertSummary("800", "800", "0", 1L, 1L, 0L);
    }

    @Test
    void totalsMultipleFeeRecordsAndPaymentsWithoutInflatingFees() {
        assertSummary("3000", "2100", "900", 3L, 1L, 2L);
    }

    @Test
    void throwsNotFoundWhenSchoolDoesNotExist() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feeRecordService.getSchoolFeeSummary(99L));

        assertEquals("School not found with ID: 99", exception.getMessage());
        verify(feeRecordRepository, never()).getSchoolFeeStats(99L);
        verify(paymentRepository, never()).sumCollectedBySchoolId(99L);
    }

    private void assertSummary(
            String totalFees,
            String totalCollected,
            String totalOutstanding,
            long totalRecords,
            long fullyPaidRecords,
            long pendingRecords) {
        School school = new School();
        school.setId(2L);
        school.setName("ABC School");
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(school));

        SchoolFeeStatsProjection statsProjection = mock(SchoolFeeStatsProjection.class);
        when(statsProjection.getTotalFees()).thenReturn(new BigDecimal(totalFees));
        when(statsProjection.getTotalFeeRecords()).thenReturn(totalRecords);
        when(statsProjection.getFullyPaidRecords()).thenReturn(fullyPaidRecords);
        when(statsProjection.getPendingRecords()).thenReturn(pendingRecords);

        when(feeRecordRepository.getSchoolFeeStats(2L)).thenReturn(statsProjection);

        when(paymentRepository.sumCollectedBySchoolId(2L)).thenReturn(new BigDecimal(totalCollected));

        SchoolFeeSummaryResponse result = feeRecordService.getSchoolFeeSummary(2L);

        assertEquals(2L, result.getSchoolId());
        assertEquals("ABC School", result.getSchoolName());
        assertEquals(new BigDecimal(totalFees), result.getTotalFees());
        assertEquals(new BigDecimal(totalCollected), result.getTotalCollected());
        assertEquals(new BigDecimal(totalOutstanding), result.getTotalOutstanding());
        assertEquals(totalRecords, result.getTotalFeeRecords());
        assertEquals(fullyPaidRecords, result.getFullyPaidRecords());
        assertEquals(pendingRecords, result.getPendingRecords());
        verify(feeRecordRepository).getSchoolFeeStats(2L);
        verify(paymentRepository).sumCollectedBySchoolId(2L);
    }
}
