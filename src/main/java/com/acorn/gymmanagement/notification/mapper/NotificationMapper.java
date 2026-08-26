package com.acorn.gymmanagement.notification.mapper;

import com.acorn.gymmanagement.notification.dto.response.NotificationResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int createMembershipNotifications();
    List<NotificationResponse> findAllByUserId(@Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly);
    List<NotificationResponse> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    int countUnreadByUserId(Long userId);
    int markRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
    int markAllRead(Long userId);
}
