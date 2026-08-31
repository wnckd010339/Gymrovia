package com.acorn.gymmanagement.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ConfirmMemberPaymentOrderRequest(
        @NotBlank(message = "결제 키가 필요합니다.")
        @Size(
                max = 200,
                message = "결제 키는 200자 이하여야 합니다."
        )
        String paymentKey,

        @NotNull(message = "결제 금액이 필요합니다.")
        @Positive(message = "결제 금액은 0원보다 커야 합니다.")
        BigDecimal amount
) {

}
