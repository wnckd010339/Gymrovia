package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.payment.dto.request.CreateRefundRequest;
import com.acorn.gymmanagement.payment.dto.response.RefundResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import com.acorn.gymmanagement.payment.model.PendingRefundCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final PaymentGateway paymentGateway;
    private final PaymentRefundTransactionService transactionService;

    public RefundResponse refund(
            Long paymentId,
            CreateRefundRequest request,
            Long processedBy
    ) {
        PendingRefundCommand command =
                transactionService.prepare(
                        paymentId,
                        request,
                        processedBy
                );

        PaymentCancellationResult result;

        try {
            result = paymentGateway.cancel(
                    command.paymentKey(),
                    command.amount(),
                    command.reason(),
                    command.idempotencyKey()
            );
        } catch (PaymentGatewayException exception) {
            transactionService.reject(
                    command.refundId(),
                    exception.getCode(),
                    exception.getMessage()
            );

            throw exception;
        }

        /*
         * Toss 취소는 성공했지만 로컬 완료 저장이 실패했다면
         * REJECTED로 바꾸지 않습니다.
         *
         * 실제 돈은 이미 환불됐을 수 있으므로 PENDING 상태를
         * 남겨 관리자가 확인할 수 있게 해야 합니다.
         */
        return transactionService.complete(
                command,
                result
        );
    }
}