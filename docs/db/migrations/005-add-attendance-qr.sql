CREATE TABLE attendance_qr_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_hash CHAR(64) NOT NULL,
    center_code VARCHAR(50) NOT NULL,
    center_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_qr_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_attendance_qr_token_status CHECK (status IN ('ACTIVE', 'EXPIRED')),
    INDEX ix_attendance_qr_token_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE attendance_qr_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    qr_token_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    verification_hash CHAR(64) NOT NULL,
    verified_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    CONSTRAINT uk_attendance_qr_verification_hash UNIQUE (verification_hash),
    CONSTRAINT fk_attendance_qr_verification_token
        FOREIGN KEY (qr_token_id) REFERENCES attendance_qr_tokens (id),
    CONSTRAINT fk_attendance_qr_verification_member
        FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT ck_attendance_qr_verification_time
        CHECK (consumed_at IS NULL OR consumed_at >= verified_at),
    INDEX ix_attendance_qr_verification_member (member_id, consumed_at, expires_at),
    INDEX ix_attendance_qr_verification_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
