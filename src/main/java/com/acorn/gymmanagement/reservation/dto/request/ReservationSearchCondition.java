package com.acorn.gymmanagement.reservation.dto.request;

import java.time.LocalDate;

public record ReservationSearchCondition(
        LocalDate weekStart,
        Long trainerId,
        String reservationType,
        String status
) {
    public LocalDate weekEnd() {
        return weekStart.plusDays(7);
    }
}
