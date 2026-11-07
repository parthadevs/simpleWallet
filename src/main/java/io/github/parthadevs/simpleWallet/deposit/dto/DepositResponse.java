package io.github.parthadevs.simpleWallet.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.parthadevs.simpleWallet.deposit.Deposit;
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
public class DepositResponse {

    private Long id;
    private BigDecimal amount;
    private String currency;
    private Deposit.DepositMethod method;
    private Deposit.DepositStatus status;
    private String reference;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("admin_note")
    private String adminNote;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}