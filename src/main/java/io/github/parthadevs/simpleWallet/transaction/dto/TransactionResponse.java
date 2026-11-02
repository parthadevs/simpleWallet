package io.github.parthadevs.simpleWallet.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.parthadevs.simpleWallet.transaction.Transaction;
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
public class TransactionResponse {

    private Long id;
    private Transaction.TransactionType type;
    private Transaction.TransactionDirection direction;
    private BigDecimal amount;
    private String currency;

    @JsonProperty("balance_before")
    private BigDecimal balanceBefore;

    @JsonProperty("balance_after")
    private BigDecimal balanceAfter;

    private Transaction.TransactionStatus status;
    private String reference;
    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}