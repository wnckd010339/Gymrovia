package com.acorn.gymmanagement.payment.gateway.toss.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResponse(
        String code,
        String message
) {
}
