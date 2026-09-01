package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.payment.dto.request.ConfirmMemberPaymentOrderRequest;
import com.acorn.gymmanagement.payment.dto.response.MemberPaymentConfirmationResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentApprovalResult;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import com.acorn.gymmanagement.payment.model.PaymentApprovalCommand;
import com.acorn.gymmanagement.payment.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberPaymentOrderServiceTest {

    private static final Long USER_ID = 10L;
    private static final Long PAYMENT_ORDER_ID = 1L;
    private static final Long MEMBERSHIP_ID = 30L;
    private static final Long PAYMENT_ID = 40L;

    private static final String ORDER_ID = "ORDER-1";
    private static final String PAYMENT_KEY =
            "test-payment-key";
    private static final String IDEMPOTENCY_KEY =
            "test-idempotency-key";

    private static final BigDecimal AMOUNT =
            new BigDecimal("100000");

    @Mock
    private MembershipService membershipService;

    @Mock
    private PaymentOrderMapper paymentOrderMapper;

    @Mock
    private PaymentOrderExpirationService expirationService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentOrderTransactionService transactionService;

    private MemberPaymentOrderService service;

    @BeforeEach
    void setUp() {
        service = new MemberPaymentOrderService(
                membershipService,
                paymentOrderMapper,
                expirationService,
                paymentGateway,
                transactionService
        );
    }

    @Test
    void confirmsPaymentAndCompletesApproval() {
        PaymentApprovalCommand command =
                approvalCommand();

        PaymentApprovalResult gatewayResult =
                approvalResult();

        MemberPaymentConfirmationResponse expected =
                confirmationResponse();

        when(transactionService.prepareApproval(
                USER_ID,
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        )).thenReturn(command);

        when(paymentGateway.confirm(
                PAYMENT_KEY,
                ORDER_ID,
                AMOUNT,
                IDEMPOTENCY_KEY
        )).thenReturn(gatewayResult);

        when(transactionService.completeApproval(
                command,
                gatewayResult
        )).thenReturn(expected);

        MemberPaymentConfirmationResponse actual =
                service.confirm(
                        USER_ID,
                        ORDER_ID,
                        confirmRequest()
                );

        assertSame(expected, actual);
        assertEquals(PAYMENT_ID, actual.paymentId());
        assertEquals(PaymentMethod.CARD, actual.paymentMethod());

        verify(transactionService).prepareApproval(
                USER_ID,
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );

        verify(paymentGateway).confirm(
                PAYMENT_KEY,
                ORDER_ID,
                AMOUNT,
                IDEMPOTENCY_KEY
        );

        verify(transactionService).completeApproval(
                command,
                gatewayResult
        );

        verify(
                transactionService,
                never()
        ).failApproval(
                PAYMENT_ORDER_ID,
                "INTERNAL_APPROVAL_ERROR",
                "결제 승인 결과를 처리하지 못했습니다."
        );
    }

    @Test
    void marksOrderFailedWhenGatewayApprovalFails() {
        PaymentApprovalCommand command =
                approvalCommand();

        PaymentGatewayException gatewayException =
                new PaymentGatewayException(
                        "REJECT_CARD_COMPANY",
                        "카드사에서 결제를 거절했습니다."
                );

        when(transactionService.prepareApproval(
                USER_ID,
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        )).thenReturn(command);

        when(paymentGateway.confirm(
                PAYMENT_KEY,
                ORDER_ID,
                AMOUNT,
                IDEMPOTENCY_KEY
        )).thenThrow(gatewayException);

        PaymentGatewayException thrown =
                assertThrows(
                        PaymentGatewayException.class,
                        () -> service.confirm(
                                USER_ID,
                                ORDER_ID,
                                confirmRequest()
                        )
                );

        assertSame(gatewayException, thrown);
        assertEquals(
                "REJECT_CARD_COMPANY",
                thrown.getCode()
        );

        verify(transactionService).failApproval(
                PAYMENT_ORDER_ID,
                "REJECT_CARD_COMPANY",
                "카드사에서 결제를 거절했습니다."
        );

        verify(
                transactionService,
                never()
        ).completeApproval(
                command,
                approvalResult()
        );

        verify(
                transactionService,
                never()
        ).prepareCompensation(
                any(PaymentApprovalCommand.class),
                any(RuntimeException.class)
        );

        verify(
                paymentGateway,
                never()
        ).cancel(
                anyString(),
                any(BigDecimal.class),
                anyString(),
                anyString()
        );

        verify(
                transactionService,
                never()
        ).requireReconciliation(
                any(Long.class),
                anyString(),
                anyString()
        );
    }

    @Test
    void compensatesPaymentWhenApprovalResultCannotBeSaved() {
        PaymentApprovalCommand command =
                approvalCommand();

        PaymentApprovalResult gatewayResult =
                approvalResult();

        BusinessException saveException =
                new BusinessException(
                        ErrorCode.INTERNAL_ERROR,
                        "결제 주문 완료 상태를 저장하지 못했습니다."
                );

        when(transactionService.prepareApproval(
                USER_ID,
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        )).thenReturn(command);

        when(paymentGateway.confirm(
                PAYMENT_KEY,
                ORDER_ID,
                AMOUNT,
                IDEMPOTENCY_KEY
        )).thenReturn(gatewayResult);

        when(transactionService.completeApproval(
                command,
                gatewayResult
        )).thenThrow(saveException);

        PaymentCancellationResult cancellationResult =
                new PaymentCancellationResult(
                        PAYMENT_KEY,
                        "compensation-transaction-key",
                        AMOUNT,
                        LocalDateTime.of(
                                2026,
                                9,
                                1,
                                10,
                                31
                        )
                );

        when(paymentGateway.cancel(
                PAYMENT_KEY,
                AMOUNT,
                "결제 승인 후 내부 처리 실패로 인한 자동 취소",
                command.compensationIdempotencyKey()
        )).thenReturn(cancellationResult);

        BusinessException thrown =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                USER_ID,
                                ORDER_ID,
                                confirmRequest()
                        )
                );

        assertSame(saveException, thrown);

        verify(transactionService).prepareCompensation(
                command,
                saveException
        );

        verify(paymentGateway).cancel(
                PAYMENT_KEY,
                AMOUNT,
                "결제 승인 후 내부 처리 실패로 인한 자동 취소",
                command.compensationIdempotencyKey()
        );

        verify(transactionService).completeCompensation(
                command,
                cancellationResult
        );

        verify(
                transactionService,
                never()
        ).failApproval(
                PAYMENT_ORDER_ID,
                "INTERNAL_APPROVAL_ERROR",
                "결제 승인 결과를 처리하지 못했습니다."
        );
    }

    @Test
    void doesNotCallGatewayWhenOrderPreparationFails() {
        BusinessException preparationException =
                new BusinessException(
                        ErrorCode.CONFLICT,
                        "결제 주문의 유효시간이 만료되었습니다."
                );

        when(transactionService.prepareApproval(
                USER_ID,
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        )).thenThrow(preparationException);

        BusinessException thrown =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                USER_ID,
                                ORDER_ID,
                                confirmRequest()
                        )
                );

        assertSame(preparationException, thrown);

        verifyNoInteractions(paymentGateway);

        verify(
                transactionService,
                never()
        ).failApproval(
                PAYMENT_ORDER_ID,
                "INTERNAL_APPROVAL_ERROR",
                "결제 승인 결과를 처리하지 못했습니다."
        );
    }

    private ConfirmMemberPaymentOrderRequest confirmRequest() {
        return new ConfirmMemberPaymentOrderRequest(
                PAYMENT_KEY,
                AMOUNT
        );
    }

    private PaymentApprovalCommand approvalCommand() {
        return new PaymentApprovalCommand(
                PAYMENT_ORDER_ID,
                ORDER_ID,
                MEMBERSHIP_ID,
                AMOUNT,
                PAYMENT_KEY,
                IDEMPOTENCY_KEY
        );
    }

    private PaymentApprovalResult approvalResult() {
        return new PaymentApprovalResult(
                PAYMENT_KEY,
                ORDER_ID,
                AMOUNT,
                "카드",
                "DONE",
                approvedAt()
        );
    }

    private MemberPaymentConfirmationResponse confirmationResponse() {
        return new MemberPaymentConfirmationResponse(
                ORDER_ID,
                PAYMENT_ID,
                AMOUNT,
                PaymentMethod.CARD,
                approvedAt()
        );
    }

    private LocalDateTime approvedAt() {
        return LocalDateTime.of(
                2026,
                9,
                1,
                10,
                30
        );
    }
}
