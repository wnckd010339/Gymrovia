package com.acorn.gymmanagement.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundRegistration {

    private Long refundId;
    private final Long paymentId;
    private final BigDecimal amount;
    private final String reason;
    private final RefundStatus status;
    private final LocalDateTime refundedAt;
    private final Long processedBy;
    private final String idempotencyKey;

    public RefundRegistration(
            Long paymentId,
            BigDecimal amount,
            String reason,
            RefundStatus status,
            LocalDateTime refundedAt,
            Long processedBy,
            String idempotencyKey
    ) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.refundedAt = refundedAt;
        this.processedBy = processedBy;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getRefundId() {
        return refundId;
    }
    public void setRefundId(Long refundId) {
        this.refundId = refundId;
    }
    public Long getPaymentId() {
        return paymentId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public String getReason() {
        return reason;
    }
    public RefundStatus getStatus() {
        return status;
    }
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    public Long getProcessedBy() {
        return processedBy;
    }
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
