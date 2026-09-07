ALTER TABLE payment_orders
    DROP CHECK ck_payment_order_status;

ALTER TABLE payment_orders
    ADD CONSTRAINT ck_payment_order_status
        CHECK (
            status IN (
                'READY',
                'APPROVING',
                'APPROVAL_UNKNOWN',
                'PAID',
                'FAILED',
                'CANCELLED',
                'EXPIRED',
                'COMPENSATING',
                'COMPENSATED',
                'RECONCILIATION_REQUIRED'
                )
            );
