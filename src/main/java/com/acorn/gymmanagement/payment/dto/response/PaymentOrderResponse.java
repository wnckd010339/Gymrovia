package com.acorn.gymmanagement.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentOrderResponse(
        String orderId,
        String orderName,
        BigDecimal amount,
        OffsetDateTime expiresAt
) {
}
