package io.github.parthadevs.simpleWallet.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Product> findByIsActiveAndCategoryId(boolean isActive, Long categoryId, Pageable pageable);

    Page<Product> findByIsActive(boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (lower(p.name) like lower(concat('%', :search, '%')) OR lower(p.slug) like lower(concat('%', :search, '%')))")
    Page<Product> searchByNameOrSlug(@Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
