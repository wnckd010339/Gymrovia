package com.acorn.gymmanagement.statistics.mapper;

import com.acorn.gymmanagement.statistics.dto.response.DailyStatisticsResponse;
import com.acorn.gymmanagement.statistics.dto.response.ProductSalesStatisticsResponse;
import com.acorn.gymmanagement.statistics.dto.response.StatisticsSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StatisticsMapper {

    StatisticsSummaryResponse findSummary(
            @Param("startDate")LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<DailyStatisticsResponse> findDailyStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<ProductSalesStatisticsResponse> findProductSales(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
