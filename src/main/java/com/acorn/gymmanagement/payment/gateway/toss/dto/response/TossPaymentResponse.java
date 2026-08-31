package com.acorn.gymmanagement.payment.gateway.toss.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        String method,
        BigDecimal totalAmount,
        OffsetDateTime approvedAt,
        List<TossCancelResponse> cancels
) {
}
