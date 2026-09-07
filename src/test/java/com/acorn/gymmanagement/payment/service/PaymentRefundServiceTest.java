package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.payment.dto.request.CreateRefundRequest;
import com.acorn.gymmanagement.payment.dto.response.RefundResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

    private PendingRefundCommand prepareRefund() {
        PendingRefundCommand command = new PendingRefundCommand(
                1L, 10L, 20L, 30L, "test_payment_key", new BigDecimal("30000"),
                "고객 요청", 100L, "refund-idempotency-key", false);
        when(transactionService.prepare(eq(10L), any(CreateRefundRequest.class), eq(100L)))
                .thenReturn(command);
        return command;
    }

    private RefundResponse refund() {
        return refundService.refund(10L, new CreateRefundRequest(new BigDecimal("30000"), "고객 요청"), 100L);
    }

    @Test
    void retainsPendingRefundWhenGatewayResultIsUnknown() {
        var command = prepareRefund();
        var exception = PaymentGatewayException.unknown("TOSS_NETWORK_ERROR", "응답 유실", null);
        when(paymentGateway.cancel(command.paymentKey(), command.amount(), command.reason(), command.idempotencyKey()))
                .thenThrow(exception);
        assertSame(exception, assertThrows(PaymentGatewayException.class, this::refund));
        verify(transactionService).keepPending(1L, "TOSS_NETWORK_ERROR", "응답 유실");
        verify(transactionService, never()).reject(any(), anyString(), anyString());
        verify(transactionService, never()).complete(any(), any());
    }

    @Test
    void rejectsRefundOnlyWhenFailureIsDefinite() {
        var command = prepareRefund();
        var exception = PaymentGatewayException.rejected("INVALID_GATEWAY_REQUEST", "잘못된 요청");
        when(paymentGateway.cancel(command.paymentKey(), command.amount(), command.reason(), command.idempotencyKey()))
                .thenThrow(exception);
        assertSame(exception, assertThrows(PaymentGatewayException.class, this::refund));
        verify(transactionService).reject(1L, exception.getCode(), exception.getMessage());
        verify(transactionService, never()).keepPending(any(), anyString(), anyString());
    }

    @Test
    void preservesPendingRefundAfterSuccessfulCancellationAndLocalFailure() {
        var command = prepareRefund();
        var cancellation = new PaymentCancellationResult(command.paymentKey(), "tx-1", command.amount(), LocalDateTime.now());
        var exception = new IllegalStateException("DB unavailable");
        when(paymentGateway.cancel(command.paymentKey(), command.amount(), command.reason(), command.idempotencyKey()))
                .thenReturn(cancellation);
        when(transactionService.complete(command, cancellation)).thenThrow(exception);
        assertSame(exception, assertThrows(IllegalStateException.class, this::refund));
        verify(transactionService).keepPending(1L, "LOCAL_REFUND_COMPLETION_FAILED",
                "PG 환불 이후 내부 저장 결과를 확인해야 합니다.");
        verify(transactionService, never()).reject(any(), anyString(), anyString());
    }

    @Test
    void preservesOriginalGatewayErrorWhenPendingReasonCannotBeSaved() {
        var command = prepareRefund();
        var exception = PaymentGatewayException.unknown("TOSS_NETWORK_ERROR", "응답 유실", null);
        var persistenceError = new IllegalStateException("DB unavailable");
        when(paymentGateway.cancel(command.paymentKey(), command.amount(), command.reason(), command.idempotencyKey()))
                .thenThrow(exception);
        doThrow(persistenceError).when(transactionService).keepPending(1L, "TOSS_NETWORK_ERROR", "응답 유실");
        assertSame(exception, assertThrows(PaymentGatewayException.class, this::refund));
        assertSame(persistenceError, exception.getSuppressed()[0]);
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
