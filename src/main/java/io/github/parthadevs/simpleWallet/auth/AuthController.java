package io.github.parthadevs.simpleWallet.auth;

import io.github.parthadevs.simpleWallet.auth.dto.AuthResponse;
import io.github.parthadevs.simpleWallet.auth.dto.LoginRequest;
import io.github.parthadevs.simpleWallet.auth.dto.RegisterRequest;
import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.user.dto.UserResponse;
import io.github.parthadevs.simpleWallet.user.dto.UserSummaryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            // Fallback safety check in case authentication principal type mismatch occurs
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User authenticatedUser) {
                user = authenticatedUser;
            } else {
                throw new ResourceNotFoundException("Unauthenticated or user principal mismatch");
            }
        }
        UserSummaryDto response = UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Authenticated user retrieved successfully.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthResponse>> logout() {
        AuthResponse response = authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully.", response));
    }
}