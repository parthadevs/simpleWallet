package io.github.parthadevs.simpleWallet.order;
import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.common.response.PageResponse;
import io.github.parthadevs.simpleWallet.order.dto.CreateOrderRequest;
import io.github.parthadevs.simpleWallet.order.dto.OrderItemResponse;
import io.github.parthadevs.simpleWallet.order.dto.OrderResponse;
import io.github.parthadevs.simpleWallet.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> listOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        Page<OrderResponse> response = orderService.listOrders(user, isAdmin, status, page, perPage);

        PageResponse.Meta meta = PageResponse.Meta.builder()
                .currentPage(page)
                .perPage(perPage)
                .total(response.getTotalElements())
                .lastPage(response.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully.",
                PageResponse.<OrderResponse>builder().data(response.getContent()).meta(meta).build()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        OrderResponse response = orderService.getOrderById(id, user, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/delivery")
    public ResponseEntity<List<OrderItemResponse>> getDelivery(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        List<OrderItemResponse> response = orderService.getDeliveryItems(id, user, isAdmin);
        return ResponseEntity.ok(response);
    }
}
