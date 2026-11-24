package io.github.parthadevs.simpleWallet.admin;

import io.github.parthadevs.simpleWallet.admin.dto.DashboardSummaryDto;
import io.github.parthadevs.simpleWallet.admin.dto.DepositReviewRequest;
import io.github.parthadevs.simpleWallet.admin.dto.UserStatusUpdateRequest;
import io.github.parthadevs.simpleWallet.admin.dto.WalletAdjustmentRequest;
import io.github.parthadevs.simpleWallet.category.dto.CategoryRequest;
import io.github.parthadevs.simpleWallet.category.dto.CategoryResponse;
import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.common.response.PageResponse;
import io.github.parthadevs.simpleWallet.deposit.dto.DepositResponse;
import io.github.parthadevs.simpleWallet.order.dto.OrderResponse;
import io.github.parthadevs.simpleWallet.payment.dto.CreatePaymentMethodRequest;
import io.github.parthadevs.simpleWallet.payment.dto.PaymentMethodResponse;
import io.github.parthadevs.simpleWallet.product.dto.CreateProductRequest;
import io.github.parthadevs.simpleWallet.product.dto.ProductResponse;
import io.github.parthadevs.simpleWallet.product.dto.ProductStockRequest;
import io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> dashboard() {
        DashboardSummaryDto response = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully.", response));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        Page<UserResponse> response = adminService.listUsers(search, status, page, perPage);
        PageResponse.Meta meta = PageResponse.Meta.builder()
                .currentPage(page)
                .perPage(perPage)
                .total(response.getTotalElements())
                .lastPage(response.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully.",
                PageResponse.<UserResponse>builder().data(response.getContent()).meta(meta).build()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        UserResponse response = adminService.getUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully.", response));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @AuthenticationPrincipal User actingAdmin,
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserResponse response = adminService.updateUserStatus(id, request, actingAdmin);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully.", response));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = adminService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully.", response));
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = adminService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", response));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable Long id) {
        adminService.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated successfully.", null));
    }

    @PostMapping("/products/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> addStock(
            @PathVariable Long id,
            @Valid @RequestBody ProductStockRequest request
    ) {
        adminService.addStock(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product stock added successfully.", null));
    }

    @GetMapping("/deposits")
    public ResponseEntity<ApiResponse<PageResponse<DepositResponse>>> listDeposits(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        Page<DepositResponse> response = adminService.listDeposits(status, page, perPage);
        PageResponse.Meta meta = PageResponse.Meta.builder()
                .currentPage(page)
                .perPage(perPage)
                .total(response.getTotalElements())
                .lastPage(response.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Deposit requests retrieved successfully.",
                PageResponse.<DepositResponse>builder().data(response.getContent()).meta(meta).build()));
    }

    @PatchMapping("/deposits/{id}/review")
    public ResponseEntity<ApiResponse<DepositResponse>> reviewDeposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositReviewRequest request
    ) {
        DepositResponse response = adminService.reviewDeposit(id, request, null);
        return ResponseEntity.ok(ApiResponse.success("Deposit reviewed successfully.", response));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        Page<OrderResponse> response = adminService.listOrders(status, page, perPage);
        PageResponse.Meta meta = PageResponse.Meta.builder()
                .currentPage(page)
                .perPage(perPage)
                .total(response.getTotalElements())
                .lastPage(response.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully.",
                PageResponse.<OrderResponse>builder().data(response.getContent()).meta(meta).build()));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        OrderResponse response = adminService.getOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully.", response));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse response = adminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully.", response));
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse response = adminService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully.", response));
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> createPaymentMethod(
            @Valid @RequestBody CreatePaymentMethodRequest request
    ) {
        PaymentMethodResponse response = adminService.createPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment method created successfully.", response));
    }

    @PatchMapping("/payment-methods/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentMethodRequest request
    ) {
        PaymentMethodResponse response = adminService.updatePaymentMethod(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment method updated successfully.", response));
    }

    @PostMapping("/users/{id}/wallet-adjustments")
    public ResponseEntity<ApiResponse<TransactionResponse>> walletAdjustment(
            @AuthenticationPrincipal User actingAdmin,
            @PathVariable Long id,
            @Valid @RequestBody WalletAdjustmentRequest request
    ) {
        TransactionResponse response = adminService.walletAdjustment(id, request, actingAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet adjustment recorded successfully.", response));
    }
}
