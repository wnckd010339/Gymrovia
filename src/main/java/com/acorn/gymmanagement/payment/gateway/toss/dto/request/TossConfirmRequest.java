package com.acorn.gymmanagement.payment.gateway.toss.dto.request;

import java.math.BigDecimal;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        BigDecimal amount
) {
}
