package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreateFeeRecordRequest;
import com.abhay.student_fee_management_module.dto.response.FeeRecordResponse;
import com.abhay.student_fee_management_module.dto.response.StudentFeeDetailsResponse;
import com.abhay.student_fee_management_module.dto.response.PagedResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Student;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.projections.FeePaymentTotalProjection;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.PaymentRepository;
import com.abhay.student_fee_management_module.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeeRecordService{

    private final FeeRecordRepository feeRecordRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public FeeRecordResponse createFeeRecord(CreateFeeRecordRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + request.getStudentId()));

        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setTitle(request.getTitle().trim());
        feeRecord.setTotalAmount(request.getTotalAmount());
        feeRecord.setDueDate(request.getDueDate());
        feeRecord.setStudent(student);

        FeeRecord savedFeeRecord = feeRecordRepository.save(feeRecord);
        return new FeeRecordResponse(
                savedFeeRecord.getId(),
                student.getId(),
                savedFeeRecord.getTitle(),
                savedFeeRecord.getTotalAmount(),
                savedFeeRecord.getDueDate());
    }

    @Transactional(readOnly = true)
    public PagedResponse<StudentFeeDetailsResponse> getStudentFees(
            Long studentId, int page, int size) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + studentId));

        Pageable pageable = PageRequest.of(page, size);

        Page<FeeRecord> feeRecordPage = feeRecordRepository.findByStudentId(studentId, pageable);
        List<FeeRecord> feeRecords = feeRecordPage.getContent();

        List<Long> feeIds = feeRecords.stream().map(k -> k.getId()).toList();

        Map<Long, BigDecimal> totalsPaidByFeeId = new HashMap<>();

        if (!feeIds.isEmpty()) {
            for (FeePaymentTotalProjection row : paymentRepository.sumAmountsByFeeRecordIds(feeIds)) {
                totalsPaidByFeeId.put(row.getFeeId(), row.getTotalPaid());
            }
        }

        List<StudentFeeDetailsResponse> responses = new ArrayList<>();
        for (FeeRecord feeRecord : feeRecords) {
            BigDecimal totalPaid = totalsPaidByFeeId.getOrDefault(feeRecord.getId(), BigDecimal.ZERO);

            BigDecimal outstandingAmount = feeRecord.getTotalAmount().subtract(totalPaid);

            responses.add(new StudentFeeDetailsResponse(
                    feeRecord.getId(),
                    feeRecord.getTitle(),
                    feeRecord.getTotalAmount(),
                    feeRecord.getDueDate(),
                    totalPaid,
                    outstandingAmount));
        }
        return new PagedResponse<>(
                responses,
                feeRecordPage.getNumber(),
                feeRecordPage.getSize(),
                feeRecordPage.getTotalElements(),
                feeRecordPage.getTotalPages(),
                feeRecordPage.isLast());
    }
}
