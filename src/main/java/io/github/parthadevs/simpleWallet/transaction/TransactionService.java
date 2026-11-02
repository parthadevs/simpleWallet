package io.github.parthadevs.simpleWallet.transaction;

import io.github.parthadevs.simpleWallet.common.exception.InsufficientBalanceException;
import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.common.exception.UnauthorizedException;
import io.github.parthadevs.simpleWallet.transaction.Transaction;
import io.github.parthadevs.simpleWallet.transaction.TransactionRepository;
import io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse;
import io.github.parthadevs.simpleWallet.transaction.dto.TransferRequest;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.user.UserRepository;
import io.github.parthadevs.simpleWallet.wallet.Wallet;
import io.github.parthadevs.simpleWallet.wallet.WalletRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse transferMoney(User sender, TransferRequest request) {
        //  Validation: Self-transfer check
        if (sender.getEmail().equalsIgnoreCase(request.getReceiverEmail())) {
            throw new IllegalArgumentException("Cannot transfer money to yourself.");
        }

        // Fetch Sender & Receiver Wallets with Lock
        Wallet senderWallet = walletRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found"));

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Wallet receiverWallet = walletRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found"));

        // Balance Validation
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance.");
        }

        // Update Balances
        BigDecimal senderBalanceBefore = senderWallet.getBalance();
        BigDecimal senderBalanceAfter = senderBalanceBefore.subtract(request.getAmount());
        senderWallet.setBalance(senderBalanceAfter);

        BigDecimal receiverBalanceBefore = receiverWallet.getBalance();
        BigDecimal receiverBalanceAfter = receiverBalanceBefore.add(request.getAmount());
        receiverWallet.setBalance(receiverBalanceAfter);

        // Save updated wallets
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Create Transaction Audit Logs (Double Entry)
        String trxReference = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction transaction = Transaction.builder()
                .user(sender)
                .wallet(senderWallet)
                .type(Transaction.TransactionType.TRANSFER)
                .direction(Transaction.TransactionDirection.OUT)
                .amount(request.getAmount())
                .balanceBefore(senderBalanceBefore)
                .balanceAfter(senderBalanceAfter)
                .status(Transaction.TransactionStatus.COMPLETED)
                .reference(trxReference)
                .description("Transfer to " + receiver.getEmail())
                .build();

        transactionRepository.save(transaction);

        return mapToTransactionResponse(transaction);
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
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

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(User currentUser, String reference) {
        // 1. Fetch transaction by reference
        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));

        // 2. Security Check: Only the transaction owner or an ADMIN can access the details
        boolean isOwner = transaction.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to view this transaction.");
        }

        // 3. Map Entity to DTO
        return mapToTransactionResponse(transaction);
    }
}