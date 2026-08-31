package com.acorn.gymmanagement.payment.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCancellationResult(
        String paymentKey,
        String transactionKey,
        BigDecimal cancelledAmount,
        LocalDateTime cancelledAt
) {
}