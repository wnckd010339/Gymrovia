package com.acorn.gymmanagement.payment.model;

public enum PaymentOrderStatus {

    READY,
    APPROVING,
    APPROVAL_UNKNOWN,

    PAID,
    FAILED,
    CANCELLED,
    EXPIRED,

    COMPENSATING,
    COMPENSATED,
    RECONCILIATION_REQUIRED
}
