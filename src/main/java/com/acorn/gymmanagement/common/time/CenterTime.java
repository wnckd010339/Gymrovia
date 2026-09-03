package com.acorn.gymmanagement.common.time;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Center business dates are independent of the JVM/container default timezone. */
public final class CenterTime {
    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private CenterTime() {}

    public static LocalDateTime now() {
        return now(Clock.systemUTC());
    }

    public static LocalDateTime now(Clock clock) {
        return LocalDateTime.ofInstant(clock.instant(), ZONE);
    }

    public static LocalDate today() {
        return now().toLocalDate();
    }
}
