package io.github.parthadevs.simpleWallet.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Items are required")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemRequest> items;

    @Size(max = 255, message = "Idempotency key must not exceed 255 characters")
    private String idempotencyKey;
}
