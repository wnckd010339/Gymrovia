package com.acorn.gymmanagement.payment.gateway.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
        String clientKey,
        String secretKey,
        String baseUrl,
        String successUrl,
        String failUrl
) {
}
