package com.abhay.student_fee_management_module.controller;

import com.abhay.student_fee_management_module.dto.request.CreateFeeRecordRequest;
import com.abhay.student_fee_management_module.dto.response.ApiResponse;
import com.abhay.student_fee_management_module.dto.response.FeeRecordResponse;
import com.abhay.student_fee_management_module.service.FeeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fees")
@RequiredArgsConstructor
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
}
