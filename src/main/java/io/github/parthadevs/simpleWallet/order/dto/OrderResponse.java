package io.github.parthadevs.simpleWallet.order.dto;

import io.github.parthadevs.simpleWallet.order.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private Long id;
    private String reference;
    private Order.OrderStatus status;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal total;
    private List<OrderItemResponse> items;
    private WalletInfo wallet;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WalletInfo {
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;
    }
}
