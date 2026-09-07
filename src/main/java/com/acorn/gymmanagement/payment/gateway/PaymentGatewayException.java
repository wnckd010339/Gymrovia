package com.acorn.gymmanagement.payment.gateway;

public class PaymentGatewayException extends RuntimeException {

    private final String code;
    private final boolean outcomeUnknown;

    public PaymentGatewayException(
            String code,
            String message
    ) {
        this(code, message, true, null);
    }

    private PaymentGatewayException(
            String code,
            String message,
            boolean outcomeUnknown,
            Throwable cause
    ) {
        super(message, cause);
        this.code = code;
        this.outcomeUnknown = outcomeUnknown;
    }

    public static PaymentGatewayException rejected(
            String code,
            String message
    ) {
        return new PaymentGatewayException(
                code,
                message,
                false,
                null
        );
    }

    public static PaymentGatewayException unknown(
            String code,
            String message,
            Throwable cause
    ) {
        return new PaymentGatewayException(
                code,
                message,
                true,
                cause
        );
    }

    public String getCode() {
        return code;
    }

    public boolean isOutcomeUnknown() {
        return outcomeUnknown;
    }
}
