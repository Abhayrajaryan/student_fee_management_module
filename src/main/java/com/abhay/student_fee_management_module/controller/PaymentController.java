package com.abhay.student_fee_management_module.controller;

import com.abhay.student_fee_management_module.dto.request.CreatePaymentRequest;
import com.abhay.student_fee_management_module.dto.response.ApiResponse;
import com.abhay.student_fee_management_module.dto.response.PaymentHistoryResponse;
import com.abhay.student_fee_management_module.dto.response.PaymentResponse;
import com.abhay.student_fee_management_module.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/recordPayment")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Payment recorded successfully",
                        response));
    }

    @GetMapping("/fees/{feeId}")
    public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getPaymentHistory(
            @PathVariable Long feeId) {

        List<PaymentHistoryResponse> payments = paymentService.getPaymentHistory(feeId);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Payment history retrieved successfully",
                payments));
    }
}
