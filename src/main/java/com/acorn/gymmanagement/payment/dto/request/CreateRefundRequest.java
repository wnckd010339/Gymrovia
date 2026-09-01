package com.acorn.gymmanagement.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateRefundRequest(
        @NotNull(message = "환불 금액을 입력해 주세요.")
        @DecimalMin(
                value = "1",
                message = "환불 금액은 1원 이상이어야 합니다."
        )
        BigDecimal amount,

        @NotBlank(message = "환불 사유를 입력해 주세요.")
        @Size(
                max = 200,
                message = "환불 사유는 200자 이내로 입력해 주세요."
        )
        String reason
) {
}
