package io.github.parthadevs.simpleWallet.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}