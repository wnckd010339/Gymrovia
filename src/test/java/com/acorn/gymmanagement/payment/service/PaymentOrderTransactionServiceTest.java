package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.payment.dto.request.CreatePaymentRequest;
import com.acorn.gymmanagement.payment.dto.response.MemberPaymentConfirmationResponse;
import com.acorn.gymmanagement.payment.dto.response.PaymentResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentApprovalResult;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import com.acorn.gymmanagement.payment.model.PaymentApprovalCommand;
import com.acorn.gymmanagement.payment.model.PaymentMethod;
import com.acorn.gymmanagement.payment.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentOrderTransactionServiceTest {

    @Mock
    private PaymentOrderMapper paymentOrderMapper;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentOrderTransactionService service;

    @Test
    void 간편결제_승인결과를_EASY_PAY로_저장한다() {
        LocalDateTime approvedAt =
                LocalDateTime.of(2026, 9, 1, 12, 0);

        PaymentApprovalCommand command =
                new PaymentApprovalCommand(
                        1L,
                        "FITFLOW-ORDER-1",
                        10L,
                        new BigDecimal("80000"),
                        "test-payment-key",
                        "test-idempotency-key"
                );

        PaymentApprovalResult result =
                new PaymentApprovalResult(
                        "test-payment-key",
                        "FITFLOW-ORDER-1",
                        new BigDecimal("80000"),
                        "간편결제",
                        "DONE",
                        approvedAt
                );

        PaymentResponse paymentResponse =
                new PaymentResponse(
                        100L,
                        20L,
                        10L,
                        "테스트 회원",
                        "1개월 자유 이용권",
                        new BigDecimal("80000"),
                        PaymentMethod.EASY_PAY,
                        PaymentStatus.COMPLETED,
                        approvedAt,
                        BigDecimal.ZERO,
                        new BigDecimal("80000")
                );

        when(paymentService.completeMembershipPayment(
                any(Long.class),
                any(CreatePaymentRequest.class)
        )).thenReturn(paymentResponse);

        when(paymentOrderMapper.markPaid(
                1L,
                100L,
                approvedAt
        )).thenReturn(1);

        MemberPaymentConfirmationResponse response =
                service.completeApproval(command, result);

        ArgumentCaptor<CreatePaymentRequest> requestCaptor =
                ArgumentCaptor.forClass(CreatePaymentRequest.class);

        verify(paymentService).completeMembershipPayment(
                org.mockito.ArgumentMatchers.eq(10L),
                requestCaptor.capture()
        );

        assertEquals(
                PaymentMethod.EASY_PAY,
                requestCaptor.getValue().paymentMethod()
        );

        assertEquals(
                PaymentMethod.EASY_PAY,
                response.paymentMethod()
        );
    }
}