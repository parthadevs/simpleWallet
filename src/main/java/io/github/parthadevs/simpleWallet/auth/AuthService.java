package io.github.parthadevs.simpleWallet.auth;

import io.github.parthadevs.simpleWallet.auth.dto.AuthResponse;
import io.github.parthadevs.simpleWallet.auth.dto.LoginRequest;
import io.github.parthadevs.simpleWallet.auth.dto.RegisterRequest;
import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.security.JwtService;
import io.github.parthadevs.simpleWallet.user.User;
import io.github.parthadevs.simpleWallet.user.UserRepository;
import io.github.parthadevs.simpleWallet.wallet.Wallet;
import io.github.parthadevs.simpleWallet.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match!");
        }

        // Create and save the new User
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Automatically provision an empty Wallet for the user
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .currency("BDT")
                .build();

        walletRepository.save(wallet);

        // Generate JWT token
        String jwtToken = jwtService.generateToken((UserDetails) savedUser);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user credentials via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Fetch user details from database
        User user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Update lastLoginAt timestamp
        user.setLastLoginAt(LocalDateTime.now());
        authRepository.save(user);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .build();
    }

    public AuthResponse logout() {
        // Returns an empty or explicit null token response to instruct client to clear storage
        return AuthResponse.builder()
                .accessToken(null)
                .tokenType(null)
                .build();
    }
}