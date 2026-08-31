package com.acorn.gymmanagement.statistics.service;

import com.acorn.gymmanagement.statistics.dto.response.DailyStatisticsResponse;
import com.acorn.gymmanagement.statistics.dto.response.ProductSalesStatisticsResponse;
import com.acorn.gymmanagement.statistics.dto.response.StatisticsPageResponse;
import com.acorn.gymmanagement.statistics.dto.response.StatisticsSummaryResponse;
import com.acorn.gymmanagement.statistics.mapper.StatisticsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsMapper statisticsMapper;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService =
                new StatisticsService(statisticsMapper);
    }

    @Test
    void createsStatisticsForCurrentAndPreviousPeriods() {
        LocalDate startDate =
                LocalDate.of(2026, 8, 1);

        LocalDate endDate =
                LocalDate.of(2026, 8, 30);

        StatisticsSummaryResponse currentSummary =
                new StatisticsSummaryResponse(
                        10,
                        8,
                        12,
                        new BigDecimal("100000"),
                        new BigDecimal("10000"),
                        new BigDecimal("90000")
                );

        StatisticsSummaryResponse previousSummary =
                new StatisticsSummaryResponse(
                        5,
                        4,
                        6,
                        new BigDecimal("50000"),
                        new BigDecimal("5000"),
                        new BigDecimal("45000")
                );

        DailyStatisticsResponse dailyStatistics =
                new DailyStatisticsResponse(
                        startDate,
                        2,
                        3,
                        4,
                        new BigDecimal("20000"),
                        new BigDecimal("2000"),
                        new BigDecimal("18000")
                );

        ProductSalesStatisticsResponse productSales =
                new ProductSalesStatisticsResponse(
                        1L,
                        "헬스 1개월",
                        2,
                        new BigDecimal("45000"),
                        0
                );

        when(statisticsMapper.findSummary(
                startDate,
                endDate
        )).thenReturn(currentSummary);

        when(statisticsMapper.findSummary(
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(previousSummary);

        when(statisticsMapper.findDailyStatistics(
                startDate,
                endDate
        )).thenReturn(List.of(dailyStatistics));

        when(statisticsMapper.findProductSales(
                startDate,
                endDate
        )).thenReturn(List.of(productSales));

        StatisticsPageResponse response =
                statisticsService.getStatistics(
                        startDate,
                        endDate
                );

        assertEquals(
                startDate,
                response.period().startDate()
        );

        assertEquals(
                endDate,
                response.period().endDate()
        );

        assertEquals(
                LocalDate.of(2026, 7, 2),
                response.period().previousStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 7, 31),
                response.period().previousEndDate()
        );

        assertSame(
                currentSummary,
                response.summary()
        );

        assertSame(
                previousSummary,
                response.previousSummary()
        );

        assertEquals(
                100,
                response.comparison().memberChangeRate()
        );

        assertEquals(
                100,
                response.comparison().visitorChangeRate()
        );

        assertEquals(
                100,
                response.comparison().attendanceChangeRate()
        );

        assertEquals(
                100,
                response.comparison().salesChangeRate()
        );

        assertEquals(
                List.of(dailyStatistics),
                response.dailyStatistics()
        );

        assertEquals(
                1,
                response.productSales().size()
        );

        assertEquals(
                50,
                response.productSales()
                        .get(0)
                        .salesRatio()
        );

        verify(statisticsMapper).findSummary(
                startDate,
                endDate
        );

        verify(statisticsMapper).findSummary(
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 31)
        );

        verify(statisticsMapper).findDailyStatistics(
                startDate,
                endDate
        );

        verify(statisticsMapper).findProductSales(
                startDate,
                endDate
        );
    }

    @Test
    void returnsZeroRatiosWhenTotalNetSalesIsZero() {
        LocalDate startDate =
                LocalDate.of(2026, 8, 1);

        LocalDate endDate =
                LocalDate.of(2026, 8, 30);

        StatisticsSummaryResponse emptySummary =
                new StatisticsSummaryResponse(
                        0,
                        0,
                        0,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        ProductSalesStatisticsResponse product =
                new ProductSalesStatisticsResponse(
                        1L,
                        "헬스 1개월",
                        1,
                        BigDecimal.ZERO,
                        0
                );

        when(statisticsMapper.findSummary(
                startDate,
                endDate
        )).thenReturn(emptySummary);

        when(statisticsMapper.findSummary(
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(emptySummary);

        when(statisticsMapper.findDailyStatistics(
                startDate,
                endDate
        )).thenReturn(List.of());

        when(statisticsMapper.findProductSales(
                startDate,
                endDate
        )).thenReturn(List.of(product));

        StatisticsPageResponse response =
                statisticsService.getStatistics(
                        startDate,
                        endDate
                );

        assertEquals(
                0,
                response.productSales()
                        .get(0)
                        .salesRatio()
        );

        assertEquals(
                0,
                response.comparison()
                        .salesChangeRate()
        );
    }

    @Test
    void calculatesNegativeChangeRates() {
        LocalDate startDate =
                LocalDate.of(2026, 8, 1);

        LocalDate endDate =
                LocalDate.of(2026, 8, 30);

        StatisticsSummaryResponse currentSummary =
                new StatisticsSummaryResponse(
                        5,
                        10,
                        20,
                        new BigDecimal("50000"),
                        new BigDecimal("10000"),
                        new BigDecimal("40000")
                );

        StatisticsSummaryResponse previousSummary =
                new StatisticsSummaryResponse(
                        10,
                        20,
                        40,
                        new BigDecimal("90000"),
                        new BigDecimal("10000"),
                        new BigDecimal("80000")
                );

        when(statisticsMapper.findSummary(
                startDate,
                endDate
        )).thenReturn(currentSummary);

        when(statisticsMapper.findSummary(
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(previousSummary);

        when(statisticsMapper.findDailyStatistics(
                startDate,
                endDate
        )).thenReturn(List.of());

        when(statisticsMapper.findProductSales(
                startDate,
                endDate
        )).thenReturn(List.of());

        StatisticsPageResponse response =
                statisticsService.getStatistics(
                        startDate,
                        endDate
                );

        assertEquals(
                -50,
                response.comparison()
                        .memberChangeRate()
        );

        assertEquals(
                -50,
                response.comparison()
                        .visitorChangeRate()
        );

        assertEquals(
                -50,
                response.comparison()
                        .attendanceChangeRate()
        );

        assertEquals(
                -50,
                response.comparison()
                        .salesChangeRate()
        );
    }

    @Test
    void returnsEmptyProductListWhenMapperReturnsNoProducts() {
        LocalDate startDate =
                LocalDate.of(2026, 8, 1);

        LocalDate endDate =
                LocalDate.of(2026, 8, 30);

        StatisticsSummaryResponse summary =
                new StatisticsSummaryResponse(
                        0,
                        0,
                        0,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        when(statisticsMapper.findSummary(
                startDate,
                endDate
        )).thenReturn(summary);

        when(statisticsMapper.findSummary(
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(summary);

        when(statisticsMapper.findDailyStatistics(
                startDate,
                endDate
        )).thenReturn(List.of());

        when(statisticsMapper.findProductSales(
                startDate,
                endDate
        )).thenReturn(List.of());

        StatisticsPageResponse response =
                statisticsService.getStatistics(
                        startDate,
                        endDate
                );

        assertEquals(
                List.of(),
                response.productSales()
        );
    }
}