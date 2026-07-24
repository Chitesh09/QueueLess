package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.dto.request.CheckInRequest;
import com.queueless.dto.request.JoinQueueRequest;
import com.queueless.dto.response.ETAResponse;
import com.queueless.dto.response.TokenResponse;
import com.queueless.queue.ETAEngine;
import com.queueless.queue.QueueEngine;
import com.queueless.queue.TokenService;
import com.queueless.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Queue Engine", description = "Join Queue, Check Token Status, QR Check-in & Cancel")
public class QueueController {

    private final QueueEngine queueEngine;
    private final TokenService tokenService;
    private final ETAEngine etaEngine;
    private final JwtTokenProvider tokenProvider;
    private final TokenRepository tokenRepository;

    public QueueController(QueueEngine queueEngine,
                           TokenService tokenService,
                           ETAEngine etaEngine,
                           JwtTokenProvider tokenProvider,
                           TokenRepository tokenRepository) {
        this.queueEngine = queueEngine;
        this.tokenService = tokenService;
        this.etaEngine = etaEngine;
        this.tokenProvider = tokenProvider;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/services/{serviceId}/tokens")
    @Operation(summary = "Join a virtual queue for a service")
    public ResponseEntity<ApiResponse<TokenResponse>> joinQueue(@PathVariable Long serviceId,
                                                                 @RequestBody(required = false) JoinQueueRequest request,
                                                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = 4L; // Default demo customer if unauthenticated
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = tokenProvider.getUserIdFromToken(authHeader.substring(7));
        }

        String priority = (request != null) ? request.getPriorityClass() : "STANDARD";
        TokenResponse response = queueEngine.joinQueue(userId, serviceId, priority);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Joined queue successfully"));
    }

    @GetMapping("/tokens/{tokenId}")
    @Operation(summary = "Get current token status & live queue position")
    public ResponseEntity<ApiResponse<TokenResponse>> getTokenStatus(@PathVariable Long tokenId) {
        Token token = tokenService.getById(tokenId);
        TokenResponse response = tokenService.mapToTokenResponse(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/services/{serviceId}/eta")
    @Operation(summary = "Get dynamic wait time ETA prediction for a position")
    public ResponseEntity<ApiResponse<ETAResponse>> getServiceETA(@PathVariable Long serviceId,
                                                                   @RequestParam(defaultValue = "1") int position) {
        ETAResponse eta = etaEngine.calculateETA(serviceId, position);
        return ResponseEntity.ok(ApiResponse.success(eta));
    }

    @PostMapping("/tokens/{tokenId}/check-in")
    @Operation(summary = "Check in to counter using HMAC QR code signature")
    public ResponseEntity<ApiResponse<TokenResponse>> checkIn(@PathVariable Long tokenId,
                                                               @Valid @RequestBody CheckInRequest request) {
        TokenResponse response = queueEngine.checkInToken(tokenId, request.getQrSignature());
        return ResponseEntity.ok(ApiResponse.success(response, "Checked in successfully"));
    }
}
