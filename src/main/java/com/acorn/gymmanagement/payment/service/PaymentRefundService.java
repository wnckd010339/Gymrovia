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
            try {
                if (exception.isOutcomeUnknown()) {
                    transactionService.keepPending(
                            command.refundId(),
                            exception.getCode(),
                            exception.getMessage()
                    );

                } else {
                    transactionService.reject(
                            command.refundId(),
                            exception.getCode(),
                            exception.getMessage()
                    );
                }

            } catch (RuntimeException persistenceException) {
                exception.addSuppressed(persistenceException);
            }

            throw exception;
        }

        try {
            return transactionService.complete(
                    command,
                    result
            );

        } catch (RuntimeException localException) {
            try {
                transactionService.keepPending(
                        command.refundId(),
                        "LOCAL_REFUND_COMPLETION_FAILED",
                        "PG 환불 이후 내부 저장 결과를 확인해야 합니다."
                );

            } catch (RuntimeException persistenceException) {
                localException.addSuppressed(
                        persistenceException
                );
            }

            throw localException;
        }
    }
}