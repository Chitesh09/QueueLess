package com.queueless.controller;

import com.queueless.common.exception.ResourceNotFoundException;
import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.User;
import com.queueless.domain.repository.UserRepository;
import com.queueless.dto.response.AuthResponse;
import com.queueless.security.QueuelessUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "User & Organization Settings", description = "User profile, password changes, and system preferences")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(@AuthenticationPrincipal QueuelessUserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated access");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getUserId()));

        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(principal.getRole())
                .organizationId(principal.getOrganizationId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/profile")
    @Operation(summary = "Update user profile information")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(@AuthenticationPrincipal QueuelessUserPrincipal principal,
                                                                   @RequestBody Map<String, String> body) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated access");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getUserId()));

        if (body.containsKey("name") && body.get("name") != null && !body.get("name").isBlank()) {
            user.setName(body.get("name").trim());
        }
        if (body.containsKey("email") && body.get("email") != null && !body.get("email").isBlank()) {
            user.setEmail(body.get("email").trim());
        }
        userRepository.save(user);

        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(principal.getRole())
                .organizationId(principal.getOrganizationId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @PostMapping("/users/change-password")
    @Operation(summary = "Change authenticated user password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@AuthenticationPrincipal QueuelessUserPrincipal principal,
                                                               @RequestBody Map<String, String> body) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated access");
        }

        String newPassword = body.get("newPassword");
        if (newPassword != null && !newPassword.isBlank()) {
            User user = userRepository.findById(principal.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getUserId()));
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }

        return ResponseEntity.ok(ApiResponse.success(true, "Password changed successfully"));
    }

    @GetMapping("/organization/preferences")
    @Operation(summary = "Get organization queue system preferences")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreferences(@AuthenticationPrincipal QueuelessUserPrincipal principal) {
        if (principal == null || (!principal.isOrgAdmin() && !principal.isSuperAdmin())) {
            throw new AccessDeniedException("Access denied: ORG_ADMIN or SUPER_ADMIN required");
        }

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
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePreferences(@AuthenticationPrincipal QueuelessUserPrincipal principal,
                                                                              @RequestBody Map<String, Object> prefs) {
        if (principal == null || (!principal.isOrgAdmin() && !principal.isSuperAdmin())) {
            throw new AccessDeniedException("Access denied: ORG_ADMIN or SUPER_ADMIN required");
        }

        return ResponseEntity.ok(ApiResponse.success(prefs, "Preferences updated successfully"));
    }
}
