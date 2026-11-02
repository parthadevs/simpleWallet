package io.github.parthadevs.simpleWallet.user;

import io.github.parthadevs.simpleWallet.common.response.ApiResponse;
import io.github.parthadevs.simpleWallet.user.dto.UpdateProfileRequest;
import io.github.parthadevs.simpleWallet.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal User user
    ) {
        UserResponse response = userService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully.", response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", response));
    }
}