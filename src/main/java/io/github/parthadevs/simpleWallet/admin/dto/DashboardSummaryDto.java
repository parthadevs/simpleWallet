package io.github.parthadevs.simpleWallet.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDto {

    private Long totalUsers;
    private Long totalProducts;
    private Long pendingDeposits;
    private Long totalOrders;
    private Long pendingOrders;
    private BigDecimal grossSales;
    private String currency;
}
