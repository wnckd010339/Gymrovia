package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.payment.dto.request.CreatePaymentRequest;
import com.acorn.gymmanagement.payment.dto.response.MemberPaymentConfirmationResponse;
import com.acorn.gymmanagement.payment.dto.response.PaymentResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentApprovalResult;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import com.acorn.gymmanagement.payment.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentOrderTransactionService {

    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentService paymentService;

    public PaymentOrderTransactionService(
            PaymentOrderMapper paymentOrderMapper,
            PaymentService paymentService
    ) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.paymentService = paymentService;
    }

    @Transactional
    public PaymentApprovalCommand prepareApproval(
            Long userId,
            String orderId,
            String paymentKey,
            BigDecimal requestedAmount
    ) {
        PaymentOrder order = paymentOrderMapper
                .findByOrderIdForUpdate(orderId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "결제 주문을 찾을 수 없습니다."
                ));

        if (order.status() != PaymentOrderStatus.READY) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "결제 대기 상태의 주문만 승인할 수 있습니다."
            );
        }

        if (order.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "결제 주문의 유효시간이 만료되었습니다."
            );
        }

        if (order.amount().compareTo(requestedAmount) != 0) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "결제 인증금액과 주문금액이 일치하지 않습니다."
            );
        }

        int affectedRows = paymentOrderMapper.markApproving(
                order.id(),
                paymentKey
        );

        if (affectedRows != 1) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "이미 처리 중이거나 상태가 변경된 주문입니다."
            );
        }

        return new PaymentApprovalCommand(
                order.id(),
                order.orderId(),
                order.membershipId(),
                order.amount(),
                paymentKey,
                order.idempotencyKey()
        );

    }

    @Transactional
    public MemberPaymentConfirmationResponse completeApproval(
            PaymentApprovalCommand command,
            PaymentApprovalResult result
    ) {
        validateGatewayResult(command, result);

        PaymentMethod paymentMethod =
                convertPaymentMethod(result.method());

        PaymentResponse payment =
                paymentService.completeMembershipPayment(
                        command.membershipId(),
                        new CreatePaymentRequest(
                                command.membershipId(),
                                paymentMethod
                        )
                );

        int affectedRows = paymentOrderMapper.markPaid(
                command.paymentOrderId(),
                payment.paymentId(),
                result.approvedAt()
        );

        if (affectedRows != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 주문 완료 상태를 저장하지 못했습니다."
            );
        }

        return new MemberPaymentConfirmationResponse(
                command.orderId(),
                payment.paymentId(),
                command.amount(),
                paymentMethod,
                result.approvedAt()
        );
    }

    @Transactional
    public void failApproval(
            Long paymentOrderId,
            String failureCode,
            String failureMessage
    ) {
        String safeMessage = failureMessage == null
                ? "결제 승인에 실패했습니다."
                : failureMessage;

        if (safeMessage.length() > 500) {
            safeMessage = safeMessage.substring(0, 500);
        }

        paymentOrderMapper.markFailed(
                paymentOrderId,
                failureCode,
                safeMessage
        );
    }

    private void validateGatewayResult(
            PaymentApprovalCommand command,
            PaymentApprovalResult result
    ) {
        if (result == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "PG 결제 승인 결과가 없습니다."
            );
        }

        if (!command.paymentKey().equals(result.paymentKey())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "PG 결제 키가 일치하지 않습니다."
            );
        }

        if (!command.orderId().equals(result.orderId())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "PG 주문번호가 일치하지 않습니다."
            );
        }

        if (result.amount() == null
                || command.amount().compareTo(
                        result.amount()
        ) != 0) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "PG 승인금액이 주문금액과 일치하지 않습니다."
            );
        }

        if (!"DONE".equals(result.status())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "결제가 승인 완료 상태가 아닙니다."
            );
        }

        if (result.approvedAt() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 승인시간을 확인할 수 없습니다."
            );
        }
    }

    private PaymentMethod convertPaymentMethod(
            String tossMethod
    ) {
        if (tossMethod == null || tossMethod.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제수단을 확인할 수 없습니다."
            );
        }

        return switch (tossMethod) {
            case "카드" -> PaymentMethod.CARD;
            case "계좌이체" -> PaymentMethod.TRANSFER;
            case "간편결제" -> PaymentMethod.EASY_PAY;

            default -> throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "지원하지 않는 결제수단입니다 :"
                        + tossMethod
            );
        };
    }

    @Transactional
    public void prepareCompensation(
            PaymentApprovalCommand command,
            RuntimeException cause
    ) {
        String message = cause.getMessage() == null
                ? "결제 승인 후 내부 저장에 실패했습니다."
                : cause.getMessage();

        if (message.length() > 500) {
            message = message.substring(0, 500);
        }

        int affectedRows =
                paymentOrderMapper.markCompensating(
                        command.paymentOrderId(),
                        command.compensationIdempotencyKey(),
                        "LOCAL_COMPLETION_FAILED",
                        message
                );

        if (affectedRows != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 보상 취소 상태를 준비하지 못했습니다."
            );
        }
    }

    @Transactional
    public void completeCompensation(
            PaymentApprovalCommand command,
            PaymentCancellationResult result
    ) {
        validateCancellationResult(command, result);

        int affectedRows =
                paymentOrderMapper.markCompensated(
                        command.paymentOrderId(),
                        result.transactionKey(),
                        result.cancelledAt()
                );

        if (affectedRows != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 보상 취소 결과를 저장하지 못했습니다."
            );
        }
    }

    @Transactional
    public void requireReconciliation(
            Long paymentOrderId,
            String failureCode,
            String failureMessage
    ) {
        String safeMessage =
                failureMessage == null
                    ? "자동 결제 취소 결과를 확인하지 못했습니다."
                    : failureMessage;

        if (safeMessage.length() > 500) {
            safeMessage = safeMessage.substring(0, 500);
        }

        int affectedRows =
                paymentOrderMapper.markReconciliationRequired(
                        paymentOrderId,
                        failureCode,
                        safeMessage
                );

        if (affectedRows != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 수동 확인 상태를 저장하지 못했습니다."
            );
        }
    }

    private void validateCancellationResult(
            PaymentApprovalCommand command,
            PaymentCancellationResult result
    ) {
        if (result == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "자동 결제 취소 결과가 없습니다."
            );
        }

        if (!command.paymentKey().equals(result.paymentKey())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "자동 취소 결과의 결제 키가 일치하지 않습니다."
            );
        }

        if (result.cancelledAmount() == null
                || command.amount().compareTo(
                        result.cancelledAmount()
        ) != 0) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "자동 취소 결과의 금액이 일치하지 않습니다."
            );
        }

        if (result.transactionKey() == null
                || result.transactionKey().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "자동 취소 거래 키가 없습니다."
            );
        }

        if (result.cancelledAt() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "자동 취소 시간을 확인하지 못했습니다."
            );
        }
    }


}
