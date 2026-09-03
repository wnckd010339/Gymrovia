package com.acorn.gymmanagement.statistics.model;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;

import java.time.LocalDate;
import com.acorn.gymmanagement.common.time.CenterTime;
import java.time.temporal.ChronoUnit;

public record StatisticsPeriod(
        LocalDate startDate,
        LocalDate endDate,
        LocalDate previousStartDate,
        LocalDate previousEndDate
) {

    private static final long MAXIMUM_DAYS = 366;

    public static StatisticsPeriod of(
            LocalDate requestedStartDate,
            LocalDate requestedEndDate
    ) {
        LocalDate endDate = requestedEndDate != null
                ? requestedEndDate
                : CenterTime.today();

        LocalDate startDate = requestedStartDate != null
                ? requestedStartDate
                : endDate.minusDays(29);

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "시작일은 종료일보다 늦을 수 없습니다."
            );
        }

        long periodDays =
                ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (periodDays > MAXIMUM_DAYS) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "통계 조회 기간은 최대 366일입니다."
            );
        }

        if (endDate.isAfter(CenterTime.today())) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "종료일은 오늘 이후일 수 없습니다."
            );
        }

        LocalDate previousEndDate =
                startDate.minusDays(1);

        LocalDate previousStartDate =
                previousEndDate.minusDays(periodDays - 1);

        return new StatisticsPeriod(
                startDate,
                endDate,
                previousStartDate,
                previousEndDate
        );
    }

    public long days() {
        return ChronoUnit.DAYS.between(
                startDate,
                endDate
        ) + 1;
    }
}
