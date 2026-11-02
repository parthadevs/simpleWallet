package io.github.parthadevs.simpleWallet.transaction;

import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse;
import io.github.parthadevs.simpleWallet.transaction.dto.TransferRequest;
import io.github.parthadevs.simpleWallet.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @AuthenticationPrincipal User user,
            @PathVariable String reference
    ) {
        TransactionResponse response = transactionService.getTransactionByReference(user, reference);
        return ResponseEntity.ok(ApiResponse.success("Transaction details retrieved successfully.", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request
    ) {
        TransactionResponse response = transactionService.transferMoney(user, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer successful.", response));
    }
}