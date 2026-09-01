package com.acorn.gymmanagement.payment.model;

public enum PaymentOrderStatus {
    READY,
    APPROVING,
    PAID,
    FAILED,
    CANCELLED,
    EXPIRED,

    COMPENSATING,
    COMPENSATED,
    RECONCILIATION_REQUIRED
}
