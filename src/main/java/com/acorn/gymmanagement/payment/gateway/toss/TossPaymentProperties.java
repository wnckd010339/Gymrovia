package com.acorn.gymmanagement.payment.gateway.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentProperties(
        String clientKey,
        String secretKey,
        String baseUrl,
        String successUrl,
        String failUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    public TossPaymentProperties {
        requirePositive(connectTimeout, "connectTimeout");
        requirePositive(readTimeout, "readTimeout");
    }

    private static void requirePositive(
            Duration duration,
            String propertyName
    ) {
        if (duration == null
                || duration.isZero()
                || duration.isNegative()) {
            throw new IllegalArgumentException(
                    propertyName + " must be greater than zero"
            );
        }
    }
}
