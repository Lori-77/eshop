package com.eshop.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class StatisticsVO {
    private long totalUsers;
    private long totalMerchants;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalSales;
    private long todayOrders;
    private BigDecimal todaySales;
    private long pendingOrders;
    private long pendingRefunds;
}
