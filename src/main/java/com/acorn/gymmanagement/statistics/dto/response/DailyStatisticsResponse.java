package com.acorn.gymmanagement.statistics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatisticsResponse(
        LocalDate statisticsDate,
        int newMemberCount,
        int uniqueVisitorCount,
        int attendanceCount,
        BigDecimal grossSales,
        BigDecimal refundAmount,
        BigDecimal netSales
) {
}
