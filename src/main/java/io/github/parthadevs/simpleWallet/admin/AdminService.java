package io.github.parthadevs.simpleWallet.admin;

import io.github.parthadevs.simpleWallet.admin.dto.DashboardSummaryDto;
import io.github.parthadevs.simpleWallet.admin.dto.DepositReviewRequest;
import io.github.parthadevs.simpleWallet.admin.dto.UserStatusUpdateRequest;
import io.github.parthadevs.simpleWallet.admin.dto.WalletAdjustmentRequest;
import io.github.parthadevs.simpleWallet.category.dto.CategoryRequest;
import io.github.parthadevs.simpleWallet.category.dto.CategoryResponse;
import io.github.parthadevs.simpleWallet.category.CategoryService;
import io.github.parthadevs.simpleWallet.common.exception.InsufficientBalanceException;
import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.deposit.Deposit;
import io.github.parthadevs.simpleWallet.deposit.DepositRepository;
import io.github.parthadevs.simpleWallet.deposit.DepositService;
import io.github.parthadevs.simpleWallet.deposit.dto.DepositResponse;
import io.github.parthadevs.simpleWallet.order.Order;
import io.github.parthadevs.simpleWallet.order.OrderRepository;
import io.github.parthadevs.simpleWallet.order.dto.OrderResponse;
import io.github.parthadevs.simpleWallet.payment.PaymentService;
import io.github.parthadevs.simpleWallet.payment.dto.CreatePaymentMethodRequest;
import io.github.parthadevs.simpleWallet.payment.dto.PaymentMethodResponse;
import io.github.parthadevs.simpleWallet.product.ProductRepository;
import io.github.parthadevs.simpleWallet.product.ProductService;
import io.github.parthadevs.simpleWallet.product.dto.CreateProductRequest;
import io.github.parthadevs.simpleWallet.product.dto.ProductResponse;
import io.github.parthadevs.simpleWallet.product.dto.ProductStockRequest;
import io.github.parthadevs.simpleWallet.transaction.Transaction;
import io.github.parthadevs.simpleWallet.transaction.TransactionRepository;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.user.UserRepository;
import io.github.parthadevs.simpleWallet.user.dto.UserResponse;
import io.github.parthadevs.simpleWallet.wallet.Wallet;
import io.github.parthadevs.simpleWallet.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final DepositRepository depositRepository;
    private final DepositService depositService;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboard() {
        Long totalUsers = userRepository.count();
        Long totalProducts = productRepository.count();
        Long pendingDeposits = depositRepository.countByStatus(Deposit.DepositStatus.PENDING);
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        BigDecimal grossSales = orderRepository.sumCompletedOrderTotals();

        return DashboardSummaryDto.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .pendingDeposits(pendingDeposits)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .grossSales(grossSales != null ? grossSales : BigDecimal.ZERO)
                .currency("BDT")
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(String search, String status, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("createdAt").descending());

        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, userPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, UserStatusUpdateRequest request, User actingAdmin) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        User.UserStatus newStatus;
        try {
            newStatus = User.UserStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: '" + request.getStatus() + "'.");
        }

        user.setStatus(newStatus);
        User updated = userRepository.save(user);
        return mapToUserResponse(updated);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        productService.deactivateProduct(id);
    }

    @Transactional
    public void addStock(Long productId, ProductStockRequest request) {
        productService.addStock(productId, request);
    }

    @Transactional(readOnly = true)
    public Page<DepositResponse> listDeposits(String status, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("createdAt").descending());

        Page<Deposit> depositPage;
        if (status != null && !status.isBlank()) {
            try {
                Deposit.DepositStatus depositStatus = Deposit.DepositStatus.valueOf(status.toUpperCase());
                depositPage = depositRepository.findByStatus(depositStatus, pageable);
            } catch (IllegalArgumentException ex) {
                depositPage = depositRepository.findAll(pageable);
            }
        } else {
            depositPage = depositRepository.findAll(pageable);
        }

        List<DepositResponse> content = depositPage.getContent().stream()
                .map(this::mapToDepositResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, depositPage.getTotalElements());
    }

    @Transactional
    public DepositResponse reviewDeposit(Long depositId, DepositReviewRequest request, User actingAdmin) {
        return depositService.reviewDepositById(depositId, request.getDecision(), request.getAdminNote());
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(String status, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("createdAt").descending());

        Page<Order> orderPage;
        if (status != null && !status.isBlank()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findAllBy(orderStatus, pageable);
            } catch (IllegalArgumentException ex) {
                orderPage = orderRepository.findAll(pageable);
            }
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(order -> mapToOrderResponse(order, null, null, null))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToOrderResponse(order, null, null, null);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @Transactional
    public PaymentMethodResponse createPaymentMethod(CreatePaymentMethodRequest request) {
        return paymentService.createPaymentMethod(request);
    }

    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Long id, CreatePaymentMethodRequest request) {
        return paymentService.updatePaymentMethod(id, request);
    }

    @Transactional
    public io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse walletAdjustment(
            Long userId, WalletAdjustmentRequest request, User actingAdmin
    ) {
        if (request.getIdempotencyKey() != null
                && transactionRepository.findByReference(request.getIdempotencyKey()).isPresent()) {
            throw new IllegalArgumentException("A wallet adjustment with this idempotency key already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        String direction = request.getDirection().toLowerCase();
        BigDecimal amount = request.getAmount();

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter;

        switch (direction) {
            case "credit" -> balanceAfter = balanceBefore.add(amount);
            case "debit" -> {
                if (balanceBefore.compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient wallet balance for adjustment.");
                }
                balanceAfter = balanceBefore.subtract(amount);
            }
            default -> throw new IllegalArgumentException(
                    "Invalid direction: '" + request.getDirection() + "'. Use 'credit' or 'debit'.");
        }

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        String trxReference = request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()
                ? request.getIdempotencyKey()
                : "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction transaction = Transaction.builder()
                .user(user)
                .wallet(wallet)
                .type(Transaction.TransactionType.ADJUSTMENT)
                .direction("credit".equals(direction)
                        ? Transaction.TransactionDirection.IN
                        : Transaction.TransactionDirection.OUT)
                .amount(amount)
                .currency(wallet.getCurrency())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(Transaction.TransactionStatus.COMPLETED)
                .reference(trxReference)
                .description("Wallet adjustment by admin: " + request.getReason()
                        + (actingAdmin != null ? " (admin: " + actingAdmin.getEmail() + ")" : ""))
                .build();

        transactionRepository.save(transaction);

        return io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .direction(transaction.getDirection())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        Wallet wallet = walletRepository.findByUserId(user.getId()).orElse(null);
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .walletBalance(wallet != null ? wallet.getBalance() : null)
                .build();
    }

    private DepositResponse mapToDepositResponse(Deposit deposit) {
        return DepositResponse.builder()
                .id(deposit.getId())
                .amount(deposit.getAmount())
                .currency(deposit.getCurrency())
                .method(deposit.getMethod())
                .status(deposit.getStatus())
                .reference(deposit.getReference())
                .paymentReference(deposit.getPaymentReference())
                .adminNote(deposit.getAdminNote())
                .createdAt(deposit.getCreatedAt())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order, List<io.github.parthadevs.simpleWallet.order.OrderItem> items,
                                             BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .currency(order.getCurrency())
                .subtotal(order.getSubtotal())
                .total(order.getTotal())
                .items(items != null ? items.stream()
                        .map(item -> io.github.parthadevs.simpleWallet.order.dto.OrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .lineTotal(item.getLineTotal())
                                .build())
                        .toList() : List.of())
                .wallet(OrderResponse.WalletInfo.builder()
                        .balanceBefore(balanceBefore)
                        .balanceAfter(balanceAfter)
                        .build())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
