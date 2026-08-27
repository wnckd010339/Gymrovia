package com.acorn.gymmanagement.reservation.dto.response;

import com.acorn.gymmanagement.reservation.model.ReservationStatus;
import com.acorn.gymmanagement.reservation.model.ReservationType;
import java.time.LocalDateTime;

public record ReservationDetailResponse(
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
        String cancellationReason
) {
}
