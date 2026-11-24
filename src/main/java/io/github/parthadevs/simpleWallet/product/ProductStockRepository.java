package io.github.parthadevs.simpleWallet.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    List<ProductStock> findByProductAndIsSoldFalse(Product product);

    List<ProductStock> findByProductIdAndIsSoldFalseOrderByCreatedAtAsc(Long productId);
}
