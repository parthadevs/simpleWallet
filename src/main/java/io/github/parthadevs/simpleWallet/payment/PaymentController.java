package io.github.parthadevs.simpleWallet.payment;

import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.payment.dto.CreatePaymentMethodRequest;
import io.github.parthadevs.simpleWallet.payment.dto.PaymentMethodResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentMethodResponse>>> listPaymentMethods(
            @RequestParam(required = false, defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15", name = "per_page") int perPage
    ) {
        Page<PaymentMethodResponse> response = paymentService.listPaymentMethods(isActive, page, perPage);
        return ResponseEntity.ok(ApiResponse.success("Payment methods retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethod(@PathVariable Long id) {
        PaymentMethodResponse response = paymentService.getPaymentMethodById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method retrieved successfully.", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> createPaymentMethod(
            @Valid @RequestBody CreatePaymentMethodRequest request
    ) {
        PaymentMethodResponse response = paymentService.createPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment method created successfully.", response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentMethodRequest request
    ) {
        PaymentMethodResponse response = paymentService.updatePaymentMethod(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment method updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(@PathVariable Long id) {
        paymentService.deletePaymentMethod(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method deactivated successfully.", null));
    }
}
