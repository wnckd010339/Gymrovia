package com.acorn.gymmanagement.statistics.controller;

import com.acorn.gymmanagement.statistics.dto.response.StatisticsComparisonResponse;
import com.acorn.gymmanagement.statistics.dto.response.StatisticsPageResponse;
import com.acorn.gymmanagement.statistics.dto.response.StatisticsSummaryResponse;
import com.acorn.gymmanagement.statistics.model.StatisticsPeriod;
import com.acorn.gymmanagement.statistics.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    private StatisticsController controller;

    @BeforeEach
    void setUp() {
        controller =
                new StatisticsController(statisticsService);
    }

    @Test
    void returnsStatisticsPageWithRequestedPeriod() {
        LocalDate startDate =
                LocalDate.of(2026, 8, 1);

        LocalDate endDate =
                LocalDate.of(2026, 8, 31);

        StatisticsPageResponse response =
                response(startDate, endDate);

        when(statisticsService.getStatistics(
                startDate,
                endDate
        )).thenReturn(response);

        ConcurrentModel model =
                new ConcurrentModel();

        String view = controller.statistics(
                startDate,
                endDate,
                model
        );

        assertEquals(
                "admin/statistics/index",
                view
        );

        assertSame(
                response,
                model.getAttribute("statistics")
        );

        verify(statisticsService).getStatistics(
                startDate,
                endDate
        );
    }

    @Test
    void allowsMissingDatesForDefaultPeriod() {
        StatisticsPageResponse response =
                response(
                        LocalDate.of(2026, 8, 2),
                        LocalDate.of(2026, 8, 31)
                );

        when(statisticsService.getStatistics(
                null,
                null
        )).thenReturn(response);

        ConcurrentModel model =
                new ConcurrentModel();

        String view = controller.statistics(
                null,
                null,
                model
        );

        assertEquals(
                "admin/statistics/index",
                view
        );

        assertSame(
                response,
                model.getAttribute("statistics")
        );

        verify(statisticsService).getStatistics(
                null,
                null
        );
    }

    private StatisticsPageResponse response(
            LocalDate startDate,
            LocalDate endDate
    ) {
        long days =
                endDate.toEpochDay()
                        - startDate.toEpochDay()
                        + 1;

        LocalDate previousEndDate =
                startDate.minusDays(1);

        LocalDate previousStartDate =
                previousEndDate.minusDays(days - 1);

        StatisticsSummaryResponse summary =
                new StatisticsSummaryResponse(
                        0,
                        0,
                        0,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        return new StatisticsPageResponse(
                new StatisticsPeriod(
                        startDate,
                        endDate,
                        previousStartDate,
                        previousEndDate
                ),
                summary,
                summary,
                new StatisticsComparisonResponse(
                        0,
                        0,
                        0,
                        0
                ),
                List.of(),
                List.of()
        );
    }
}