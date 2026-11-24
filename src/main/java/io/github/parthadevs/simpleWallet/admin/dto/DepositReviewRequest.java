package io.github.parthadevs.simpleWallet.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositReviewRequest {

    @NotBlank(message = "Decision is required")
    private String decision;

    private String adminNote;
}
