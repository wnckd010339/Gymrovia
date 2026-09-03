package com.acorn.gymmanagement.statistics.model;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatisticsPeriodTest {
    @Test
    void createsSameLengthPreviousPeriod() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 30)
        );

        assertEquals(
                LocalDate.of(2026, 7, 2),
                period.previousStartDate()
        );
        assertEquals(
                LocalDate.of(2026, 7, 31),
                period.previousEndDate()
        );
    }

    @Test
    void usesThirtyDaysWhenStartDateIsMissing() {
        LocalDate endDate =
                LocalDate.of(2026, 8, 30);

        StatisticsPeriod period =
                StatisticsPeriod.of(
                        null,
                        endDate
                );

        assertEquals(
                LocalDate.of(2026, 8, 1),
                period.startDate()
        );

        assertEquals(
                endDate,
                period.endDate()
        );

        assertEquals(
                30,
                period.days()
        );
    }

    @Test
    void rejectsStartDateAfterEndDate() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> StatisticsPeriod.of(
                                LocalDate.of(2026, 8, 31),
                                LocalDate.of(2026, 8, 1)
                        )
                );

        assertEquals(
                ErrorCode.VALIDATION_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsPeriodsLongerThanMaximumDays(){
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> StatisticsPeriod.of(
                                LocalDate.of(2025, 1, 1),
                                LocalDate.of(2026, 1, 2)
                        )
                );

        assertEquals(
                ErrorCode.VALIDATION_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsFutureEndDate() {
        LocalDate futureDate =
                com.acorn.gymmanagement.common.time.CenterTime.today().plusDays(1);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> StatisticsPeriod.of(
                                futureDate.minusDays(6),
                                futureDate
                        )
                );

        assertEquals(
                ErrorCode.VALIDATION_ERROR,
                exception.getErrorCode()
        );
    }

}
