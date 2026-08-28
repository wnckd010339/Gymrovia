package com.acorn.gymmanagement.attendance.model;

import java.time.LocalDateTime;

public record AttendanceQrVerification(
        Long verificationId,
        Long memberId,
        String centerCode,
        String centerName,
        LocalDateTime expiresAt
) {
}
