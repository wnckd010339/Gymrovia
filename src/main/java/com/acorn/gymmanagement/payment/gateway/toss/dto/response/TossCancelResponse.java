package com.acorn.gymmanagement.payment.gateway.toss.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossCancelResponse(
        String transactionKey,
        BigDecimal cancelAmount,
        String cancelStatus,
        OffsetDateTime canceledAt
) {
}
