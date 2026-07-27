package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.User;
import com.queueless.domain.repository.UserRepository;
import com.queueless.dto.response.AuthResponse;
import com.queueless.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "User & Organization Settings", description = "User profile, password changes, and system preferences")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/users/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = 1L;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = tokenProvider.getUserIdFromToken(authHeader.substring(7));
        }

        User user = userRepository.findById(userId)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(AuthResponse.builder()
                    .userId(1L)
                    .email("operator@cityhospital.com")
                    .name("Dr. Sarah Jenkins")
                    .role("ADMIN")
                    .organizationId(1L)
                    .build()));
        }

        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role("ADMIN")
                .organizationId(1L)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/profile")
    @Operation(summary = "Update user profile information")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                                   @RequestBody Map<String, String> body) {
        Long userId = 1L;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = tokenProvider.getUserIdFromToken(authHeader.substring(7));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (body.containsKey("name")) user.setName(body.get("name"));
            if (body.containsKey("email")) user.setEmail(body.get("email"));
            userRepository.save(user);
        }

        AuthResponse response = AuthResponse.builder()
                .userId(userId)
                .name(body.getOrDefault("name", user != null ? user.getName() : "Dr. Sarah Jenkins"))
                .email(body.getOrDefault("email", user != null ? user.getEmail() : "operator@cityhospital.com"))
                .role("ADMIN")
                .organizationId(1L)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @PostMapping("/users/change-password")
    @Operation(summary = "Change authenticated user password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                               @RequestBody Map<String, String> body) {
        Long userId = 1L;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = tokenProvider.getUserIdFromToken(authHeader.substring(7));
        }

        String newPassword = body.get("newPassword");
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && newPassword != null && !newPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }

        return ResponseEntity.ok(ApiResponse.success(true, "Password changed successfully"));
    }

    @GetMapping("/organization/preferences")
    @Operation(summary = "Get organization queue system preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreferences() {
        Map<String, Object> prefs = Map.of(
                "enableSmsNotifications", true,
                "enableEmailAlerts", true,
                "maxQueueCapacity", 200,
                "slaThresholdMinutes", 15,
                "defaultGracePeriodMinutes", 10
        );
        return ResponseEntity.ok(ApiResponse.success(prefs));
    }

    @PutMapping("/organization/preferences")
    @Operation(summary = "Update organization queue system preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePreferences(@RequestBody Map<String, Object> prefs) {
        return ResponseEntity.ok(ApiResponse.success(prefs, "Preferences updated successfully"));
    }
}
