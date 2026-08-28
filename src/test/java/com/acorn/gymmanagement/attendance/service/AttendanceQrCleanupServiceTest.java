package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.mapper.AttendanceQrMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceQrCleanupServiceTest {

    @Mock
    private AttendanceQrMapper mapper;

    @Test
    void expiresTokensAndDeletesOldVerifications() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 28, 16, 0);
        when(mapper.expireQrTokens(now)).thenReturn(3);
        when(mapper.deleteExpiredVerifications(now.minusDays(1))).thenReturn(2);

        int cleaned = new AttendanceQrCleanupService(mapper).cleanup(now);

        assertEquals(5, cleaned);
        verify(mapper).expireQrTokens(now);
        verify(mapper).deleteExpiredVerifications(now.minusDays(1));
    }
}
