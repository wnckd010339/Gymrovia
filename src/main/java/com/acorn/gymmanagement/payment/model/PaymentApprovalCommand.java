package com.acorn.gymmanagement.payment.model;

import java.math.BigDecimal;

public record PaymentApprovalCommand(
        Long paymentOrderId,
        String orderId,
        Long membershipId,
        BigDecimal amount,
        String paymentKey,
        String idempotencyKey
) {
    public String compensationIdempotencyKey() {
        return "COMPENSATION-" + idempotencyKey;
    }
}
