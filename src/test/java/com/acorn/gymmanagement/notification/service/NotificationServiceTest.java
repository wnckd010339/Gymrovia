package com.acorn.gymmanagement.notification.service;

import com.acorn.gymmanagement.notification.dto.response.NotificationResponse;
import com.acorn.gymmanagement.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationMapper notificationMapper;
    @InjectMocks NotificationService notificationService;

    @Test
    void headerSynchronizesThenReturnsUnreadCountAndRecentItems() {
        NotificationResponse item = new NotificationResponse(
                1L, "MEMBERSHIP_EXPIRY", "회원권 만료", "3일 남았습니다.",
                "/member/memberships", null, LocalDateTime.now()
        );
        when(notificationMapper.countUnreadByUserId(7L)).thenReturn(1);
        when(notificationMapper.findRecentByUserId(7L, 5)).thenReturn(List.of(item));

        NotificationService.NotificationHeaderView result = notificationService.header(7L);

        assertThat(result.unreadCount()).isEqualTo(1);
        assertThat(result.recent()).containsExactly(item);
        var ordered = inOrder(notificationMapper);
        ordered.verify(notificationMapper).createMembershipNotifications();
        ordered.verify(notificationMapper).countUnreadByUserId(7L);
        ordered.verify(notificationMapper).findRecentByUserId(7L, 5);
    }

    @Test
    void readOperationsAreScopedToCurrentUser() {
        notificationService.markRead(11L, 7L);
        notificationService.markAllRead(7L);

        verify(notificationMapper).markRead(11L, 7L);
        verify(notificationMapper).markAllRead(7L);
    }
}
