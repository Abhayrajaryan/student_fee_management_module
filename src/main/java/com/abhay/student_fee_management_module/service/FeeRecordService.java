package com.abhay.student_fee_management_module.service;

import com.abhay.student_fee_management_module.dto.request.CreateFeeRecordRequest;
import com.abhay.student_fee_management_module.dto.response.FeeRecordResponse;
import com.abhay.student_fee_management_module.entity.FeeRecord;
import com.abhay.student_fee_management_module.entity.Student;
import com.abhay.student_fee_management_module.exception.ResourceNotFoundException;
import com.abhay.student_fee_management_module.repository.FeeRecordRepository;
import com.abhay.student_fee_management_module.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeeRecordService{

    private final FeeRecordRepository feeRecordRepository;
    private final StudentRepository studentRepository;

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
}
