package com.acorn.gymmanagement.statistics.dto.response;

public record StatisticsComparisonResponse(
        int memberChangeRate,
        int visitorChangeRate,
        int attendanceChangeRate,
        int salesChangeRate
) {
}
