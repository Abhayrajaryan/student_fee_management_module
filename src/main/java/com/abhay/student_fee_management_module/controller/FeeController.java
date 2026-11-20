package com.abhay.student_fee_management_module.controller;

import com.abhay.student_fee_management_module.dto.request.CreateFeeRecordRequest;
import com.abhay.student_fee_management_module.dto.response.ApiResponse;
import com.abhay.student_fee_management_module.dto.response.FeeRecordResponse;
import com.abhay.student_fee_management_module.dto.response.StudentFeeDetailsResponse;
import com.abhay.student_fee_management_module.service.FeeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.abhay.student_fee_management_module.dto.response.PagedResponse;

@RestController
@RequestMapping("/fees")
@RequiredArgsConstructor
@Validated
public class FeeController {

    private final FeeRecordService feeRecordService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<FeeRecordResponse>> createFeeRecord(
            @Valid @RequestBody CreateFeeRecordRequest request) {
        FeeRecordResponse response = feeRecordService.createFeeRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Fee record created successfully",
                        response));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<PagedResponse<StudentFeeDetailsResponse>>> getStudentFees(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        PagedResponse<StudentFeeDetailsResponse> fees = feeRecordService.getStudentFees(studentId, page, size);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Student fee details retrieved successfully",
                fees));
    }
}
