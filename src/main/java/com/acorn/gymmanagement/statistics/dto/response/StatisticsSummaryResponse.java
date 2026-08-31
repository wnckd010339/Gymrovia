package com.acorn.gymmanagement.statistics.dto.response;

import java.math.BigDecimal;

public record StatisticsSummaryResponse(
        int newMemberCount,
        int uniqueVisitorCount,
        int totalAttendanceCount,
        BigDecimal grossSales,
        BigDecimal refundAmount,
        BigDecimal netSales
) {
}
