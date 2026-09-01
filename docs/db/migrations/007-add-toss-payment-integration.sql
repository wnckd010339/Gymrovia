ALTER TABLE refunds
    MODIFY refunded_at DATETIME NULL,
    ADD COLUMN pg_transaction_key VARCHAR(64) NULL
    AFTER processed_by,
    ADD COLUMN idempotency_key VARCHAR(300) NULL
    AFTER pg_transaction_key,
    ADD COLUMN failure_code VARCHAR(100) NULL
    AFTER idempotency_key,
    ADD COLUMN failure_message VARCHAR(500) NULL
    AFTER failure_code,
    ADD CONSTRAINT uk_refund_pg_transaction_key
    UNIQUE (pg_transaction_key),
    ADD CONSTRAINT uk_refund_idempotency_key
    UNIQUE (idempotency_key),
    ADD INDEX ix_refunds_payment_status
    (payment_id, status);