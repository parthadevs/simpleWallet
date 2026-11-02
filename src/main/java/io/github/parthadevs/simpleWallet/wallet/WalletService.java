package io.github.parthadevs.simpleWallet.wallet;

import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.common.response.PageResponse;
import io.github.parthadevs.simpleWallet.transaction.Transaction;
import io.github.parthadevs.simpleWallet.transaction.TransactionRepository;
import io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public WalletResponse getWallet(User currentUser) {
        Wallet wallet = walletRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + currentUser.getId()));

        return WalletResponse.builder()
                .currency(wallet.getCurrency())
                .balance(wallet.getBalance())
                .availableBalance(wallet.getBalance())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getWalletTransactions(User currentUser, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("createdAt").descending());

        Page<Transaction> transactionPage = transactionRepository.findByUserId(currentUser.getId(), pageable);

        List<TransactionResponse> content = transactionPage.getContent().stream()
                .map(this::mapToTransactionResponse)
                .toList();

        PageResponse.Meta meta = PageResponse.Meta.builder()
                .currentPage(page)
                .perPage(perPage)
                .total(transactionPage.getTotalElements())
                .lastPage(transactionPage.getTotalPages())
                .build();

        return PageResponse.<TransactionResponse>builder()
                .data(content)
                .meta(meta)
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .type(txn.getType())
                .direction(txn.getDirection())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .balanceBefore(txn.getBalanceBefore())
                .balanceAfter(txn.getBalanceAfter())
                .status(txn.getStatus())
                .reference(txn.getReference())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}