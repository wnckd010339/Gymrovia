package com.acorn.gymmanagement.payment.model;

import java.math.BigDecimal;

public record RefundPaymentTarget(
        Long paymentId,
        Long memberId,
        Long membershipId,
        String paymentKey,
        BigDecimal paymentAmount,
        BigDecimal refundedAmount,
        PaymentStatus paymentStatus
) {
    public BigDecimal refundableAmount() {
        return paymentAmount.subtract(refundedAmount);
    }
}
