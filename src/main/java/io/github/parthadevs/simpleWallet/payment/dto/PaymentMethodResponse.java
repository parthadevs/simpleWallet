package io.github.parthadevs.simpleWallet.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {

    private Long id;
    private String name;
    private String slug;
    private String type;
    private String instructions;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private String currency;
    private boolean isActive;
    private LocalDateTime createdAt;
}
