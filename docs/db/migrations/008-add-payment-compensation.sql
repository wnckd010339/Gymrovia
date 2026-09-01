ALTER TABLE payment_orders
DROP CHECK ck_payment_order_status;

ALTER TABLE payment_orders
    ADD COLUMN compensation_idempotency_key VARCHAR(300) NULL
        AFTER approved_at,
    ADD COLUMN compensation_transaction_key VARCHAR(64) NULL
        AFTER compensation_idempotency_key,
    ADD COLUMN compensated_at DATETIME NULL
        AFTER compensation_transaction_key,
    ADD CONSTRAINT uk_payment_order_compensation_idempotency
        UNIQUE (compensation_idempotency_key),
    ADD CONSTRAINT uk_payment_order_compensation_transaction
        UNIQUE (compensation_transaction_key),
    ADD CONSTRAINT ck_payment_order_status CHECK (
        status IN (
            'READY',
            'APPROVING',
            'PAID',
            'FAILED',
            'CANCELLED',
            'EXPIRED',
            'COMPENSATING',
            'COMPENSATED',
            'RECONCILIATION_REQUIRED'
        )
    );