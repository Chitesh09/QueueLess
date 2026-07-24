package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.User;
import com.queueless.domain.entity.UserRole;
import com.queueless.domain.repository.UserRepository;
import com.queueless.domain.repository.UserRoleRepository;
import com.queueless.dto.request.AuthRequest;
import com.queueless.dto.response.AuthResponse;
import com.queueless.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, Registration & Token Management")
public class AuthController {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserRepository userRepository,
                          UserRoleRepository userRoleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        List<UserRole> roles = userRoleRepository.findByUserId(user.getId());
        String roleStr = roles.isEmpty() ? "USER" : roles.get(0).getRole();
        Long orgId = roles.isEmpty() ? 1L : roles.get(0).getOrganizationId();

        String jwt = tokenProvider.generateToken(
                user.getEmail(),
                user.getId(),
                orgId,
                roleStr
        );

        AuthResponse response = AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .organizationId(orgId)
                .name(user.getName())
                .email(user.getEmail())
                .role(roleStr)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}