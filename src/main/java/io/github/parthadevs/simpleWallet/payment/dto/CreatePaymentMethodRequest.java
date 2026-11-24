package io.github.parthadevs.simpleWallet.payment.dto;

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
public class CreatePaymentMethodRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters")
    private String slug;

    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    @Size(max = 1000, message = "Instructions must not exceed 1000 characters")
    private String instructions;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.00", message = "Minimum amount must not be negative")
    private BigDecimal minimumAmount;

    @NotNull(message = "Maximum amount is required")
    @DecimalMin(value = "0.00", message = "Maximum amount must not be negative")
    private BigDecimal maximumAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    private Boolean isActive;
}
