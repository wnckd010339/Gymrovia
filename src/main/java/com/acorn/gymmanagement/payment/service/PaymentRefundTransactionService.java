package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.membership.mapper.MembershipMapper;
import com.acorn.gymmanagement.payment.dto.request.CreateRefundRequest;
import com.acorn.gymmanagement.payment.dto.response.RefundResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.mapper.PaymentMapper;
import com.acorn.gymmanagement.payment.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRefundTransactionService {

    private final PaymentMapper paymentMapper;
    private final MembershipMapper membershipMapper;

    @Transactional
    public PendingRefundCommand prepare(
            Long paymentId,
            CreateRefundRequest request,
            Long processedBy
    ) {
        if (processedBy == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "관리자 로그인 정보가 필요합니다."
            );
        }

        RefundPaymentTarget target = paymentMapper
                .findRefundTargetForUpdate(paymentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "결제 내역을 찾을 수 없습니다."
                ));

        validatePaymentStatus(target.paymentStatus());

        if (target.paymentKey() == null
                || target.paymentKey().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "PG로 처리한 결제만 실제 환불할 수 있습니다."
            );
        }

        if (paymentMapper.existsPendingRefundByPaymentId(paymentId)) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "이미 처리 중인 환불 요청이 있습니다."
            );
        }

        BigDecimal refundAmount = request.amount();
        BigDecimal refundableAmount =
                target.refundableAmount();

        if (refundAmount == null
                || refundAmount.signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "환불 금액은 0원보다 커야 합니다."
            );
        }

        if (refundAmount.stripTrailingZeros().scale() > 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "원화 환불 금액에는 소수점을 사용할 수 없습니다."
            );
        }

        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "환불 가능 금액을 초과했습니다."
            );
        }

        String reason = request.reason().trim();
        String idempotencyKey =
                UUID.randomUUID().toString();

        RefundRegistration registration =
                new RefundRegistration(
                        paymentId,
                        refundAmount,
                        reason,
                        RefundStatus.PENDING,
                        null,
                        processedBy,
                        idempotencyKey
                );

        if (paymentMapper.insertRefund(registration) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "환불 요청을 저장하지 못했습니다."
            );
        }

        boolean fullRefund =
                refundAmount.compareTo(refundableAmount) == 0;

        return new PendingRefundCommand(
                registration.getRefundId(),
                paymentId,
                target.memberId(),
                target.membershipId(),
                target.paymentKey(),
                refundAmount,
                reason,
                processedBy,
                idempotencyKey,
                fullRefund
        );
    }

    @Transactional
    public RefundResponse complete(
            PendingRefundCommand command,
            PaymentCancellationResult result
    ) {
        validateGatewayResult(command, result);

        if (paymentMapper.completeRefund(
                command.refundId(),
                result.transactionKey(),
                result.cancelledAt()
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "환불 상태가 변경되어 완료 처리하지 못했습니다."
            );
        }

        PaymentStatus paymentStatus =
                command.fullRefund()
                        ? PaymentStatus.REFUNDED
                        : PaymentStatus.PARTIALLY_REFUNDED;

        if (paymentMapper.updatePaymentStatus(
                command.paymentId(),
                paymentStatus
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 상태를 변경하지 못했습니다."
            );
        }

        /*
         * 전액 환불이면 사용 중인 회원권을 취소합니다.
         * 이미 만료되거나 취소된 회원권은 변경 행이 0일 수 있으므로,
         * 여기서는 PG 환불 전체를 롤백시키지 않습니다.
         */
        if (command.fullRefund()
                && command.membershipId() != null) {
            membershipMapper.cancelAfterFullRefund(
                    command.memberId(),
                    command.membershipId()
            );
        }

        return paymentMapper
                .findRefundById(command.refundId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_ERROR,
                        "완료된 환불 내역을 조회하지 못했습니다."
                ));
    }

    @Transactional
    public void reject(
            Long refundId,
            String failureCode,
            String failureMessage
    ) {
        String safeCode =
                failureCode == null || failureCode.isBlank()
                        ? "REFUND_FAILED"
                        : failureCode;

        String safeMessage =
                failureMessage == null || failureMessage.isBlank()
                        ? "결제 취소 요청에 실패했습니다."
                        : failureMessage;

        if (safeMessage.length() > 500) {
            safeMessage = safeMessage.substring(0, 500);
        }

        if (paymentMapper.rejectRefund(
                refundId,
                safeCode,
                safeMessage
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "환불 실패 상태를 저장하지 못했습니다."
            );
        }
    }

    private void validatePaymentStatus(
            PaymentStatus status
    ) {
        if (status != PaymentStatus.COMPLETED
                && status != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "환불 가능한 결제 상태가 아닙니다."
            );
        }
    }

    private void validateGatewayResult(
            PendingRefundCommand command,
            PaymentCancellationResult result
    ) {
        if (result == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "PG 환불 결과가 없습니다."
            );
        }

        if (!command.paymentKey().equals(
                result.paymentKey()
        )) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "PG 환불 결과의 결제 키가 일치하지 않습니다."
            );
        }

        if (result.cancelledAmount() == null
                || command.amount().compareTo(
                result.cancelledAmount()
        ) != 0) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "PG 환불 결과의 금액이 일치하지 않습니다."
            );
        }

        if (result.transactionKey() == null
                || result.transactionKey().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "PG 환불 거래 키가 없습니다."
            );
        }

        if (result.cancelledAt() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "PG 환불 완료 시간을 확인할 수 없습니다."
            );
        }
    }
}