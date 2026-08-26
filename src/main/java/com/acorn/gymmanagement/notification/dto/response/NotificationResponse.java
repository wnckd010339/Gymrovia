package com.acorn.gymmanagement.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String targetUrl,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public boolean unread() {
        return readAt == null;
    }
}
