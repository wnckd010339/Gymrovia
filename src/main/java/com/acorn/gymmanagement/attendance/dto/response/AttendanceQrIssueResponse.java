package com.acorn.gymmanagement.attendance.dto.response;

public record AttendanceQrIssueResponse(
        String imageDataUrl,
        long expiresInSeconds,
        String centerName
) {
}
