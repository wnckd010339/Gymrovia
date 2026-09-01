package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.payment.dto.request.CreateRefundRequest;
import com.acorn.gymmanagement.payment.dto.response.RefundResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.model.PendingRefundCommand;
import com.acorn.gymmanagement.payment.model.RefundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRefundServiceTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentRefundTransactionService transactionService;

    private PaymentRefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new PaymentRefundService(
                paymentGateway,
                transactionService
        );
    }

    @Test
    void refundCallsGatewayAndCompletesTransaction() {
        CreateRefundRequest request =
                new CreateRefundRequest(
                        new BigDecimal("30000"),
                        "고객 요청"
                );

        PendingRefundCommand command =
                new PendingRefundCommand(
                        1L,
                        10L,
                        20L,
                        30L,
                        "test_payment_key",
                        new BigDecimal("30000"),
                        "고객 요청",
                        100L,
                        "refund-idempotency-key",
                        false
                );

        PaymentCancellationResult cancellation =
                new PaymentCancellationResult(
                        "test_payment_key",
                        "transaction-key",
                        new BigDecimal("30000"),
                        LocalDateTime.of(
                                2026, 9, 1, 12, 0
                        )
                );

        RefundResponse expected =
                new RefundResponse(
                        1L,
                        10L,
                        new BigDecimal("30000"),
                        "고객 요청",
                        RefundStatus.COMPLETED,
                        cancellation.cancelledAt(),
                        100L
                );

        when(transactionService.prepare(
                10L, request, 100L
        )).thenReturn(command);

        when(paymentGateway.cancel(
                command.paymentKey(),
                command.amount(),
                command.reason(),
                command.idempotencyKey()
        )).thenReturn(cancellation);

        when(transactionService.complete(
                command,
                cancellation
        )).thenReturn(expected);

        RefundResponse result =
                refundService.refund(
                        10L,
                        request,
                        100L
                );

        assertEquals(expected, result);

        verify(paymentGateway).cancel(
                command.paymentKey(),
                command.amount(),
                command.reason(),
                command.idempotencyKey()
        );

        verify(transactionService).complete(
                command,
                cancellation
        );
    }
}
