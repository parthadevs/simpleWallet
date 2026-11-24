package io.github.parthadevs.simpleWallet.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<PaymentMethod> findByIsActive(boolean isActive, Pageable pageable);
}
