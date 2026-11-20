package com.abhay.student_fee_management_module.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PendingStudentResponse {
    private final Long studentId;
    private final String studentName;
    private final Long schoolId;
    private final String schoolName;
    private final List<PendingFeeResponse> pendingFees;
}
