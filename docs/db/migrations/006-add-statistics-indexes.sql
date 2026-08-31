CREATE INDEX ix_members_joined_at
    ON members (joined_at);

CREATE INDEX ix_attendances_attendance_date
    ON attendances (attendance_date);

CREATE INDEX ix_payments_paid_at_status
    ON payments (paid_at, status);

CREATE INDEX ix_refunds_refunded_at_status
    ON refunds (refunded_at, status);




