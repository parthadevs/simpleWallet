package io.github.parthadevs.simpleWallet.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);

    Page<Order> findAllBy(Order.OrderStatus status, Pageable pageable);

    Optional<Order> findByReference(String reference);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal sumCompletedOrderTotals();

    Long countByStatus(Order.OrderStatus status);
}
