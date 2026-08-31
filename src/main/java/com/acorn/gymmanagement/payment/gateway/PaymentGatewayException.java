package com.acorn.gymmanagement.payment.gateway;

public class PaymentGatewayException extends RuntimeException {

    private final String code;

    public PaymentGatewayException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
