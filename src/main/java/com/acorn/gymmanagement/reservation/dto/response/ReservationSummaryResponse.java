package com.acorn.gymmanagement.reservation.dto.response;

public record ReservationSummaryResponse(
        int consultationCount,
        int trialPtCount,
        int regularPtCount,
        int cancelledOrNoShowCount,
        int pendingCount
) {
}
