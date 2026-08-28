package com.acorn.gymmanagement.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "attendance.qr",
        name = "cleanup-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AttendanceQrCleanupScheduler {

    private final AttendanceQrCleanupService cleanupService;

    @Scheduled(
            fixedDelayString =
                    "${attendance.qr.cleanup-delay-ms:60000}"
    )
    public void cleanup() {
        int cleanedCount =
                cleanupService.cleanup(
                        LocalDateTime.now()
                );

        if (cleanedCount > 0) {
            log.info(
                    "QR 출석 만료 데이터 {}건을 정리했습니다.",
                    cleanedCount
            );
        }
    }
}
