package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.Token;
import com.queueless.dto.request.CheckInRequest;
import com.queueless.dto.request.JoinQueueRequest;
import com.queueless.dto.response.ETAResponse;
import com.queueless.dto.response.TokenResponse;
import com.queueless.queue.ETAEngine;
import com.queueless.queue.QueueEngine;
import com.queueless.queue.TokenService;
import com.queueless.security.GuestRateLimiter;
import com.queueless.security.GuestSessionService;
import com.queueless.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final GuestSessionService guestSessionService;
    private final GuestRateLimiter guestRateLimiter;

    public QueueController(QueueEngine queueEngine,
                           TokenService tokenService,
                           ETAEngine etaEngine,
                           JwtTokenProvider tokenProvider,
                           GuestSessionService guestSessionService,
                           GuestRateLimiter guestRateLimiter) {
        this.queueEngine = queueEngine;
        this.tokenService = tokenService;
        this.etaEngine = etaEngine;
        this.tokenProvider = tokenProvider;
        this.guestSessionService = guestSessionService;
        this.guestRateLimiter = guestRateLimiter;
    }

    @PostMapping("/services/{serviceId}/tokens")
    @Operation(summary = "Join a virtual queue for a service")
    public ResponseEntity<ApiResponse<TokenResponse>> joinQueue(@PathVariable Long serviceId,
                                                                 @RequestBody(required = false) JoinQueueRequest request,
                                                                 @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                                 @RequestHeader(value = "X-Guest-Token", required = false) String guestTokenHeader,
                                                                 @RequestHeader(value = "X-Guest-ID", required = false) String legacyGuestIdHeader,
                                                                 HttpServletRequest httpRequest,
                                                                 HttpServletResponse httpResponse) {
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenProvider.validateToken(token)) {
                userId = tokenProvider.getUserIdFromToken(token);
            }
        }

        String guestSessionId = null;
        String issuedGuestToken = null;

        if (userId == null) {
            // Apply IP-based rate limiting on unauthenticated guest joins to mitigate automated queue flooding
            String clientIp = httpRequest != null ? httpRequest.getHeader("X-Forwarded-For") : null;
            if (clientIp == null || clientIp.isBlank()) {
                clientIp = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";
            } else if (clientIp.contains(",")) {
                clientIp = clientIp.split(",")[0].trim();
            }
            guestRateLimiter.checkRateLimit(clientIp);

            // Cryptographically verify or generate signed guest session from header or cookie
            Cookie[] cookies = httpRequest != null ? httpRequest.getCookies() : null;
            String tokenCandidate = guestSessionService.extractTokenCandidate(guestTokenHeader, legacyGuestIdHeader, cookies);

            GuestSessionService.GuestSession session = guestSessionService.resolveOrCreateGuestSession(tokenCandidate);
            guestSessionId = session.guestId();
            issuedGuestToken = session.signedToken();
        }

        String priority = (request != null) ? request.getPriorityClass() : "STANDARD";
        TokenResponse tokenResponse = queueEngine.joinQueue(userId, guestSessionId, serviceId, priority);

        var responseBuilder = ResponseEntity.status(HttpStatus.CREATED);
        if (issuedGuestToken != null) {
            responseBuilder.header("X-Guest-Token", issuedGuestToken);
            if (httpResponse != null) {
                Cookie cookie = new Cookie("queueless_guest_token", issuedGuestToken);
                cookie.setPath("/");
                cookie.setMaxAge(86400); // 24 hours
                cookie.setHttpOnly(true);
                httpResponse.addCookie(cookie);
            }
        }
        return responseBuilder.body(ApiResponse.success(tokenResponse, "Joined queue successfully"));
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
