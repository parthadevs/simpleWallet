package io.github.parthadevs.simpleWallet.deposit;

import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.common.response.PageResponse;
import io.github.parthadevs.simpleWallet.deposit.dto.CreateDepositRequest;
import io.github.parthadevs.simpleWallet.deposit.dto.DepositResponse;
import io.github.parthadevs.simpleWallet.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    public ResponseEntity<ApiResponse<DepositResponse>> createDeposit(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateDepositRequest request
    ) {
        DepositResponse response = depositService.createDeposit(user, request);
        return ResponseEntity.ok(ApiResponse.success("Deposit request submitted successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepositResponse>>> getDeposits(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        PageResponse<DepositResponse> response = depositService.getDeposits(user, page, perPage);
        return ResponseEntity.ok(ApiResponse.success("Deposits retrieved successfully.", response));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<DepositResponse>> getDepositDetails(
            @AuthenticationPrincipal User user,
            @PathVariable String reference
    ) {
        DepositResponse response = depositService.getDepositDetails(user, reference);
        return ResponseEntity.ok(ApiResponse.success("Deposit details retrieved successfully.", response));
    }

    // Admin Endpoint to Approve Deposit
    @PostMapping("/{reference}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepositResponse>> approveDeposit(
            @PathVariable String reference,
            @RequestParam(required = false, name = "admin_note") String adminNote
    ) {
        DepositResponse response = depositService.approveDeposit(reference, adminNote);
        return ResponseEntity.ok(ApiResponse.success("Deposit approved and wallet credited successfully.", response));
    }
}