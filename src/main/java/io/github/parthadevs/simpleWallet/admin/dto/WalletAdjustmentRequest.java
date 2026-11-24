package io.github.parthadevs.simpleWallet.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletAdjustmentRequest {

    @NotBlank(message = "Direction is required")
    private String direction;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must not be negative")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;

    @Size(max = 255, message = "Idempotency key must not exceed 255 characters")
    private String idempotencyKey;
}
