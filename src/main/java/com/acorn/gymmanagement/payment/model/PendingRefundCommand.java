package com.acorn.gymmanagement.payment.model;

import java.math.BigDecimal;

public record PendingRefundCommand(
        Long refundId,
        Long paymentId,
        Long memberId,
        Long membershipId,
        String paymentKey,
        BigDecimal amount,
        String reason,
        Long processedBy,
        String idempotencyKey,
        boolean fullRefund
) {
}
