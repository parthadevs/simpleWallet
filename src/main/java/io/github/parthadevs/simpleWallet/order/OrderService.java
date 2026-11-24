package io.github.parthadevs.simpleWallet.order;

import io.github.parthadevs.simpleWallet.common.exception.InsufficientBalanceException;
import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.order.dto.CreateOrderRequest;
import io.github.parthadevs.simpleWallet.order.dto.OrderItemRequest;
import io.github.parthadevs.simpleWallet.order.dto.OrderItemResponse;
import io.github.parthadevs.simpleWallet.order.dto.OrderResponse;
import io.github.parthadevs.simpleWallet.product.Product;
import io.github.parthadevs.simpleWallet.product.ProductRepository;
import io.github.parthadevs.simpleWallet.product.ProductStock;
import io.github.parthadevs.simpleWallet.product.ProductStockRepository;
import io.github.parthadevs.simpleWallet.transaction.Transaction;
import io.github.parthadevs.simpleWallet.transaction.TransactionRepository;
import io.github.parthadevs.simpleWallet.user.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final WalletRepository walletRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public OrderResponse createOrder(User currentUser, CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        if (request.getIdempotencyKey() != null
                && orderRepository.findByReference(request.getIdempotencyKey()).isPresent()) {
            throw new IllegalArgumentException("A purchase with this idempotency key already exists.");
        }

        Wallet wallet = walletRepository.findByUserIdWithLock(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + currentUser.getId()));

        List<OrderItemRequest> items = request.getItems();
        List<Long> productIds = new ArrayList<>();
        for (OrderItemRequest item : items) {
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for every order item.");
            }
            productIds.add(item.getProductId());
        }
        productIds.sort(Long::compareTo);

        List<Product> products = new ArrayList<>();
        for (Long productId : productIds) {
            Product product = productRepository.findByIdWithLock(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            products.add(product);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemResponse> orderItems = new ArrayList<>();
        for (OrderItemRequest item : items) {
            Product product = findProduct(products, item.getProductId());

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product '" + product.getName() + "' is not available.");
            }

            if (product.getAvailableQuantity() == null || product.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product '" + product.getName() + "'.");
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            orderItems.add(OrderItemResponse.builder()
                    .productId(product.getId().toString())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build());
        }

        BigDecimal total = subtotal;

        if (wallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance.");
        }

        reserveStock(products, items);

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(total);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        String reference = request.getIdempotencyKey() != null ? request.getIdempotencyKey()
                : "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .user(currentUser)
                .reference(reference)
                .status(Order.OrderStatus.COMPLETED)
                .currency(wallet.getCurrency())
                .subtotal(subtotal)
                .total(total)
                .deliveryStatus(Order.DeliveryStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> savedItems = new ArrayList<>();
        for (OrderItemResponse item : orderItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .lineTotal(item.getLineTotal())
                    .build();
            savedItems.add(orderItemRepository.save(orderItem));
        }

        String trxReference = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .wallet(wallet)
                .type(Transaction.TransactionType.PAYMENT)
                .direction(Transaction.TransactionDirection.OUT)
                .amount(total)
                .currency(wallet.getCurrency())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(Transaction.TransactionStatus.COMPLETED)
                .reference(trxReference)
                .description("Purchase order " + reference)
                .build();
        transactionRepository.save(transaction);

        return mapToOrderResponse(savedOrder, savedItems, balanceBefore, balanceAfter);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, User currentUser, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        if (!isOwner && !isAdmin) {
            throw new io.github.parthadevs.simpleWallet.common.exception.UnauthorizedException(
                    "You do not have permission to view this order.");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .currency(order.getCurrency())
                .subtotal(order.getSubtotal())
                .total(order.getTotal())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(User currentUser, boolean isAdmin, String status, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("createdAt").descending());

        Page<Order> orderPage;
        if (status != null && !status.isBlank()) {
            Order.OrderStatus orderStatus;
            try {
                orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                orderStatus = Order.OrderStatus.COMPLETED;
            }
            if (isAdmin) {
                orderPage = orderRepository.findAllBy(orderStatus, pageable);
            } else {
                orderPage = orderRepository.findByUserIdAndStatus(currentUser.getId(), orderStatus, pageable);
            }
        } else if (isAdmin) {
            orderPage = orderRepository.findAll(pageable);
        } else {
            orderPage = orderRepository.findByUserId(currentUser.getId(), pageable);
        }

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(order -> mapToOrderResponse(order, orderItemRepository.findByOrderId(order.getId()),
                        null, null))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getDeliveryItems(Long orderId, User currentUser, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        if (!isOwner && !isAdmin) {
            throw new io.github.parthadevs.simpleWallet.common.exception.UnauthorizedException(
                    "You do not have permission to view this order's delivery.");
        }

        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order is not completed; no delivery available.");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return items.stream().map(this::mapToItemResponse).toList();
    }

    private Product findProduct(List<Product> products, Long productId) {
        return products.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private void reserveStock(List<Product> products, List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            Product product = findProduct(products, item.getProductId());
            product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
            productRepository.save(product);

            List<ProductStock> stockRows = productStockRepository.findByProductIdAndIsSoldFalseOrderByCreatedAtAsc(
                    product.getId());
            int toSell = item.getQuantity();
            int sold = 0;
            for (ProductStock stock : stockRows) {
                if (sold >= toSell) {
                    break;
                }
                stock.setSold(true);
                stock.setSoldAt(java.time.LocalDateTime.now());
                stock.setSoldToOrderId(null);
                productStockRepository.save(stock);
                sold++;
            }
        }
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> items,
            BigDecimal balanceBefore, BigDecimal balanceAfter) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .currency(order.getCurrency())
                .subtotal(order.getSubtotal())
                .total(order.getTotal())
                .items(itemResponses)
                .wallet(OrderResponse.WalletInfo.builder()
                        .balanceBefore(balanceBefore)
                        .balanceAfter(balanceAfter)
                        .build())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
