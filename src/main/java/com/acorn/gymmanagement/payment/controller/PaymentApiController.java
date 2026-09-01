package com.acorn.gymmanagement.payment.controller;

import com.acorn.gymmanagement.common.response.ApiResponse;
import com.acorn.gymmanagement.payment.dto.request.CreatePaymentRequest;
import com.acorn.gymmanagement.payment.dto.request.CreateRefundRequest;
import com.acorn.gymmanagement.payment.dto.response.PaymentHistoryResponse;
import com.acorn.gymmanagement.payment.dto.response.PaymentResponse;
import com.acorn.gymmanagement.payment.dto.response.RefundResponse;
import com.acorn.gymmanagement.payment.service.PaymentRefundService;
import com.acorn.gymmanagement.payment.service.PaymentService;
import com.acorn.gymmanagement.security.SessionUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final PaymentRefundService paymentRefundService;

    @GetMapping("/api/payments")
    public ApiResponse<List<PaymentHistoryResponse>> findHistory(
            @RequestParam(required = false) Long memberId
    ) {
        return ApiResponse.success(
                "결제·환불 내역을 조회했습니다.",
                paymentService.findHistory(memberId)
        );
    }

    @PostMapping("/api/payments/{paymentId}/refunds")
    public ResponseEntity<ApiResponse<RefundResponse>> refund(
            @PathVariable Long paymentId,
            @Valid @RequestBody CreateRefundRequest request,
            @SessionAttribute(SessionUser.SESSION_KEY) SessionUser sessionUser
    ) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "환불이 완료되었습니다.",
                        paymentRefundService.refund(
                                paymentId,
                                request,
                                sessionUser.userId()
                        )
                ));
    }

    @PostMapping("/api/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @Valid @RequestBody CreatePaymentRequest request,
            @SessionAttribute(SessionUser.SESSION_KEY) SessionUser sessionUser
            ) {
        PaymentResponse response =
                paymentService.completeMembershipPayment(
                        request.membershipId(),
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "결제가 완료되었습니다.",
                        response
                ));
    }
}
