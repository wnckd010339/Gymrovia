package com.acorn.gymmanagement.reservation.model;

import java.time.LocalDateTime;

public record ReservationRegistration(
        Long reservationId,
        Long memberId,
        Long trainerId,
        String customerName,
        String customerPhone,
        ReservationType reservationType,
        ReservationStatus status,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String memo,
        Long createdBy
) {
}
