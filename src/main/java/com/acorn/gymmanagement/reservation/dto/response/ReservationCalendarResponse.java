package com.acorn.gymmanagement.reservation.dto.response;

import com.acorn.gymmanagement.reservation.model.ReservationStatus;
import com.acorn.gymmanagement.reservation.model.ReservationType;

import java.time.LocalDateTime;

public record ReservationCalendarResponse(
        Long reservationId,
        Long memberId,
        Long trainerId,
        String customerName,
        String customerPhone,
        String trainerName,
        ReservationType reservationType,
        ReservationStatus status,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String memo
) {
    public String reservationTypeLabel() {
        return reservationType.getLabel();
    }

    public String statusLabel() {
        return status.getLabel();
    }
}
