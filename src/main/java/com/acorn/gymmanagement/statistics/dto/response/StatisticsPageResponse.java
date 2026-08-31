package com.acorn.gymmanagement.statistics.dto.response;

import com.acorn.gymmanagement.statistics.model.StatisticsPeriod;

import java.util.List;

public record StatisticsPageResponse(
        StatisticsPeriod period,
        StatisticsSummaryResponse summary,
        StatisticsSummaryResponse previousSummary,
        StatisticsComparisonResponse comparison,
        List<DailyStatisticsResponse> dailyStatistics,
        List<ProductSalesStatisticsResponse> productSales
) {
}
