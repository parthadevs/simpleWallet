package io.github.parthadevs.simpleWallet.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.parthadevs.simpleWallet.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;

    @JsonProperty("profile_image")
    private String profileImage;

    private User.UserStatus status;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("last_login_at")
    private LocalDateTime lastLoginAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}