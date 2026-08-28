package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.mapper.AttendanceQrMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceQrCleanupService {

    private final AttendanceQrMapper attendanceQrMapper;

    @Transactional
    public int cleanup(LocalDateTime now) {
        int expiredTokenCount =
                attendanceQrMapper.expireQrTokens(now);

        int deletedVerificationCount =
                attendanceQrMapper
                        .deleteExpiredVerifications(
                                now.minusDays(1)
                        );

        return expiredTokenCount
                + deletedVerificationCount;
    }
}
