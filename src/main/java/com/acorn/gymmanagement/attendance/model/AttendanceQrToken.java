package com.acorn.gymmanagement.attendance.model;

import java.time.LocalDateTime;

public record AttendanceQrToken(
        Long qrTokenId,
        String centerCode,
        String centerName,
        LocalDateTime expiresAt
) {
}
