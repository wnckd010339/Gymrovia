package com.acorn.gymmanagement.payment.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentApprovalResult(
        String paymentKey,
        String orderId,
        BigDecimal amount,
        String method,
        String status,
        LocalDateTime approvedAt
){

}
