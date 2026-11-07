package io.github.parthadevs.simpleWallet.deposit;

import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.common.exception.UnauthorizedException;
import io.github.parthadevs.simpleWallet.common.response.PageResponse;
import io.github.parthadevs.simpleWallet.deposit.dto.CreateDepositRequest;
import io.github.parthadevs.simpleWallet.deposit.dto.DepositResponse;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public DepositResponse createDeposit(User currentUser, CreateDepositRequest request) {
        Wallet wallet = walletRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + currentUser.getId()));

        String reference = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Deposit deposit = Deposit.builder()
                .user(currentUser)
                .wallet(wallet)
                .amount(request.getAmount())
                .currency(wallet.getCurrency())
                .method(request.getMethod())
                .status(Deposit.DepositStatus.PENDING)
                .reference(reference)
                .paymentReference(request.getPaymentReference())
                .build();

        Deposit savedDeposit = depositRepository.save(deposit);
        return mapToDepositResponse(savedDeposit);
    }

    @Transactional(readOnly = true)
    public PageResponse<DepositResponse> getDeposits(User currentUser, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("createdAt").descending());

        Page<Deposit> depositPage = depositRepository.findByUserId(currentUser.getId(), pageable);

        List<DepositResponse> content = depositPage.getContent().stream()
                .map(this::mapToDepositResponse)
                .toList();

        PageResponse.Meta meta = PageResponse.Meta.builder()
                .currentPage(page)
                .perPage(perPage)
                .total(depositPage.getTotalElements())
                .lastPage(depositPage.getTotalPages())
                .build();

        return PageResponse.<DepositResponse>builder()
                .data(content)
                .meta(meta)
                .build();
    }

    @Transactional(readOnly = true)
    public DepositResponse getDepositDetails(User currentUser, String reference) {
        Deposit deposit = depositRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit request not found with reference: " + reference));

        boolean isOwner = deposit.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to view this deposit request.");
        }

        return mapToDepositResponse(deposit);
    }

    // ==========================================
    // Admin Only: Approval Logic
    // ==========================================
    @Transactional
    public DepositResponse approveDeposit(String reference, String adminNote) {
        Deposit deposit = depositRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit request not found with reference: " + reference));

        if (deposit.getStatus() != Deposit.DepositStatus.PENDING) {
            throw new IllegalStateException("Deposit request is already processed with status: " + deposit.getStatus());
        }

        Wallet wallet = deposit.getWallet();

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(deposit.getAmount());

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        deposit.setStatus(Deposit.DepositStatus.APPROVED);
        deposit.setAdminNote(adminNote);
        Deposit updatedDeposit = depositRepository.save(deposit);

        // Record Audit Transaction (Double Entry)
        String trxReference = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction transaction = Transaction.builder()
                .user(deposit.getUser())
                .wallet(wallet)
                .type(Transaction.TransactionType.DEPOSIT)
                .direction(Transaction.TransactionDirection.IN)
                .amount(deposit.getAmount())
                .currency(deposit.getCurrency())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(Transaction.TransactionStatus.COMPLETED)
                .reference(trxReference)
                .description("Deposit approved (" + deposit.getMethod() + ") Ref: " + deposit.getReference())
                .build();

        transactionRepository.save(transaction);

        return mapToDepositResponse(updatedDeposit);
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
}