package io.github.parthadevs.simpleWallet.deposit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    Page<Deposit> findByUserId(Long userId, Pageable pageable);

    Optional<Deposit> findByReference(String reference);

    Page<Deposit> findByStatus(Deposit.DepositStatus status, Pageable pageable);
}