package com.acorn.gymmanagement.notification.service;

import com.acorn.gymmanagement.notification.dto.response.NotificationResponse;
import com.acorn.gymmanagement.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;

    @Transactional
    public void synchronizeMembershipNotifications() {
        notificationMapper.createMembershipNotifications();
    }

    @Transactional
    public List<NotificationResponse> findAll(Long userId, boolean unreadOnly) {
        synchronizeMembershipNotifications();
        return notificationMapper.findAllByUserId(userId, unreadOnly);
    }

    @Transactional
    public NotificationHeaderView header(Long userId) {
        synchronizeMembershipNotifications();
        return new NotificationHeaderView(
                notificationMapper.countUnreadByUserId(userId),
                notificationMapper.findRecentByUserId(userId, 5)
        );
    }

    public void markRead(Long notificationId, Long userId) {
        notificationMapper.markRead(notificationId, userId);
    }

    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    public record NotificationHeaderView(int unreadCount, List<NotificationResponse> recent) {}
}
