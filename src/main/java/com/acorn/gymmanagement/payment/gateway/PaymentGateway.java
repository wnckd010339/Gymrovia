package com.acorn.gymmanagement.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentApprovalResult confirm(
            String paymentKey,
            String orderId,
            BigDecimal amount,
            String idempotencyKey
    );

    PaymentCancellationResult cancel(
            String paymentKey,
            BigDecimal amount,
            String reason,
            String idempotencyKey
    );
}