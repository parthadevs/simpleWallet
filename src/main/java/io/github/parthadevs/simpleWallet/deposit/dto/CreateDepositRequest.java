package io.github.parthadevs.simpleWallet.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.parthadevs.simpleWallet.deposit.Deposit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDepositRequest {

    @NotNull(message = "Deposit amount is required")
    @DecimalMin(value = "10.00", message = "Minimum deposit amount is 10.00 BDT")
    private BigDecimal amount;

    @NotNull(message = "Deposit method is required")
    @JsonProperty("payment_method_id")
    private Deposit.DepositMethod method;

    @JsonProperty("sender_account")
    private String senderAccount;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("proof_url")
    private String proofUrl;
}