package io.github.parthadevs.simpleWallet.wallet;

import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.common.response.PageResponse;
import io.github.parthadevs.simpleWallet.transaction.dto.TransactionResponse;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @AuthenticationPrincipal User user
    ) {
        WalletResponse response = walletService.getWallet(user);
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully.", response));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getWalletTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        PageResponse<TransactionResponse> response = walletService.getWalletTransactions(user, page, perPage);
        return ResponseEntity.ok(ApiResponse.success("Wallet transactions retrieved successfully.", response));
    }
}