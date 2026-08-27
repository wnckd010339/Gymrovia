CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NULL,
    trainer_id BIGINT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    reservation_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    memo VARCHAR(1000) NULL,
    cancellation_reason VARCHAR(500) NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_reservation_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id),
    CONSTRAINT fk_reservation_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_reservation_type CHECK (
        reservation_type IN ('CONSULTATION', 'TRIAL_PT', 'REGULAR_PT')
    ),
    CONSTRAINT ck_reservation_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')
    ),
    CONSTRAINT ck_reservation_period CHECK (ends_at > starts_at),
    INDEX ix_reservation_period (starts_at, ends_at),
    INDEX ix_reservation_trainer_period (trainer_id, starts_at, ends_at),
    INDEX ix_reservation_member (member_id, starts_at),
    INDEX ix_reservation_status (status, starts_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
