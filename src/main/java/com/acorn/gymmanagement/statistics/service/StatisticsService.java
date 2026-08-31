package com.acorn.gymmanagement.statistics.service;

import com.acorn.gymmanagement.statistics.dto.response.*;
import com.acorn.gymmanagement.statistics.mapper.StatisticsMapper;
import com.acorn.gymmanagement.statistics.model.StatisticsPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsPageResponse getStatistics(
            LocalDate requestedStartDate,
            LocalDate requestedEndDate
    ) {
        StatisticsPeriod period =
                StatisticsPeriod.of(
                        requestedStartDate,
                        requestedEndDate
                );

        StatisticsSummaryResponse summary =
                statisticsMapper.findSummary(
                        period.startDate(),
                        period.endDate()
                );

        StatisticsSummaryResponse previousSummary =
                statisticsMapper.findSummary(
                        period.previousStartDate(),
                        period.previousEndDate()
                );

        List<DailyStatisticsResponse> dailyStatistics =
                statisticsMapper.findDailyStatistics(
                        period.startDate(),
                        period.endDate()
                );

        List<ProductSalesStatisticsResponse> productSales =
                addSalesRatio(
                        statisticsMapper.findProductSales(
                                period.startDate(),
                                period.endDate()
                        ),
                        summary.netSales()
                );

        StatisticsComparisonResponse comparison =
                new StatisticsComparisonResponse(
                        calculateChangeRate(
                                summary.newMemberCount(),
                                previousSummary.newMemberCount()
                        ),
                        calculateChangeRate(
                                summary.uniqueVisitorCount(),
                                previousSummary.uniqueVisitorCount()
                        ),
                        calculateChangeRate(
                                summary.totalAttendanceCount(),
                                previousSummary.totalAttendanceCount()
                        ),
                        calculateChangeRate(
                                summary.netSales(),
                                previousSummary.netSales()
                        )
                );

        return new StatisticsPageResponse(
                period,
                summary,
                previousSummary,
                comparison,
                dailyStatistics,
                productSales
        );
    }

    private int calculateChangeRate(
            int current,
            int previous
    ) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }

        return (int) Math.round(
                (current - previous) * 100.0 / previous
        );
    }

    private int calculateChangeRate(
            BigDecimal current,
            BigDecimal previous
    ) {
        if (previous == null ||
                previous.compareTo(BigDecimal.ZERO) == 0) {
            return current == null ||
                    current.compareTo(BigDecimal.ZERO) == 0
                    ? 0
                    : 100;
        }

        return current
                .subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private List<ProductSalesStatisticsResponse> addSalesRatio(
            List<ProductSalesStatisticsResponse> products,
            BigDecimal totalNetSales
    ) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        if (totalNetSales == null ||
                totalNetSales.compareTo(BigDecimal.ZERO) <= 0) {
            return products.stream()
                    .map(product ->
                            new ProductSalesStatisticsResponse(
                                    product.productId(),
                                    product.productName(),
                                    product.paymentCount(),
                                    product.netSales(),
                                    0
                            )
                    )
                    .toList();
        }
        return products.stream()
                .map(product -> new ProductSalesStatisticsResponse(
                        product.productId(),
                        product.productName(),
                        product.paymentCount(),
                        product.netSales(),
                        product.netSales()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                        totalNetSales,
                                        0,
                                        RoundingMode.HALF_UP
                                )
                                .intValue()
                ))
                .toList();
    }

}
