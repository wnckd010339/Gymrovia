package com.acorn.gymmanagement.statistics.dto.response;

import java.math.BigDecimal;

public record ProductSalesStatisticsResponse(
        Long productId,
        String productName,
        int paymentCount,
        BigDecimal netSales,
        int salesRatio
) {
}
