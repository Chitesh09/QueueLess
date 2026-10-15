package com.queueless.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class GuestSessionSecurityTest {

    private GuestSessionService guestSessionService;
    private static final String TEST_SECRET = "test-secret-key-for-unit-testing-queueless-2026";

    @BeforeEach
    public void setUp() {
        guestSessionService = new GuestSessionService();
        ReflectionTestUtils.setField(guestSessionService, "secret", TEST_SECRET);
    }

    @Test
    @DisplayName("Fresh session creation returns valid cryptographically signed guest token")
    public void testCreateNewGuestSession() {
        GuestSessionService.GuestSession session = guestSessionService.createNewGuestSession();
        assertNotNull(session);
        assertNotNull(session.guestId());
        assertTrue(session.guestId().startsWith("guest_"));
        assertNotNull(session.signedToken());
        assertTrue(session.isNew());

        // Validate the newly issued token parses and extracts the exact same guest ID
        String extracted = guestSessionService.validateAndExtractGuestId(session.signedToken());
        assertEquals(session.guestId(), extracted);
    }

    @Test
    @DisplayName("Resolving an authentic signed token preserves guest identity across requests")
    public void testAuthenticTokenPreservesIdentity() {
        GuestSessionService.GuestSession initial = guestSessionService.createNewGuestSession();

        // Customer returns with their signed token on a subsequent request
        GuestSessionService.GuestSession resolved = guestSessionService.resolveOrCreateGuestSession(initial.signedToken());

        assertFalse(resolved.isNew(), "Resolved session must not be flagged as new");
        assertEquals(initial.guestId(), resolved.guestId(), "Guest ID must match the original authentic session");
        assertEquals(initial.signedToken(), resolved.signedToken());
    }

    @Test
    @DisplayName("Arbitrary client-chosen spoofed guest ID is rejected and replaced with new session")
    public void testSpoofedGuestIdIsRejected() {
        // Attacker attempts to impersonate another guest by sending raw string "victim_123"
        GuestSessionService.GuestSession session = guestSessionService.resolveOrCreateGuestSession("victim_123");

        assertTrue(session.isNew(), "Spoofed attempt must trigger a newly issued session");
        assertNotEquals("victim_123", session.guestId(), "Server must never adopt arbitrary client-chosen ID");
        assertTrue(session.guestId().startsWith("guest_"));
    }

    @Test
    @DisplayName("Tampered payload in signed token fails HMAC verification and is rejected")
    public void testTamperedSignatureIsRejected() {
        GuestSessionService.GuestSession initial = guestSessionService.createNewGuestSession();
        String originalToken = initial.signedToken();
        String[] parts = originalToken.split("\\.");
        assertEquals(3, parts.length);

        // Attacker alters the guest ID part while keeping the signature
        String tamperedToken = "guest_attacker." + parts[1] + "." + parts[2];

        GuestSessionService.GuestSession resolved = guestSessionService.resolveOrCreateGuestSession(tamperedToken);

        assertTrue(resolved.isNew(), "Tampered token must trigger a newly issued session");
        assertNotEquals("guest_attacker", resolved.guestId());
    }

    @Test
    @DisplayName("Expired guest token fails validation and issues new session")
    public void testExpiredGuestTokenIsRejected() {
        // Create a token expired 1 hour ago
        String expiredToken = guestSessionService.createSignedTokenForGuest("guest_expired_user", -3600000L);

        GuestSessionService.GuestSession resolved = guestSessionService.resolveOrCreateGuestSession(expiredToken);

        assertTrue(resolved.isNew(), "Expired token must trigger a newly issued session");
        assertNotEquals("guest_expired_user", resolved.guestId());
    }

    @Test
    @DisplayName("Replay of expired guest token fails and cannot impersonate prior identity")
    public void testReplayOfExpiredTokenFails() {
        String expiredToken = guestSessionService.createSignedTokenForGuest("guest_replay_victim", -1000L);
        String extracted = guestSessionService.validateAndExtractGuestId(expiredToken);
        assertNull(extracted, "Expired token replay must fail validation and extract null");
    }

    @Test
    @DisplayName("Cookie fallback resolves signed token when X-Guest-Token header is absent")
    public void testCookieFallbackResolution() {
        GuestSessionService.GuestSession initial = guestSessionService.createNewGuestSession();
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("queueless_guest_token", initial.signedToken());

        String extractedCandidate = guestSessionService.extractTokenCandidate(null, null, new jakarta.servlet.http.Cookie[]{cookie});
        assertEquals(initial.signedToken(), extractedCandidate);

        GuestSessionService.GuestSession resolved = guestSessionService.resolveOrCreateGuestSession(extractedCandidate);
        assertEquals(initial.guestId(), resolved.guestId());
    }

    @Test
    @DisplayName("GuestRateLimiter permits normal requests and blocks when threshold is exceeded")
    public void testGuestRateLimiterThreshold() {
        GuestRateLimiter limiter = new GuestRateLimiter();
        limiter.resetForTesting();
        String testIp = "192.168.1.100";

        // First 10 requests must succeed
        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> limiter.checkRateLimit(testIp));
        }

        // 11th request must throw BusinessException (Rate limit exceeded)
        com.queueless.common.exception.BusinessException ex = assertThrows(
                com.queueless.common.exception.BusinessException.class,
                () -> limiter.checkRateLimit(testIp)
        );
        assertTrue(ex.getMessage().contains("Rate limit exceeded"));

        // Different IP address is not affected
        assertDoesNotThrow(() -> limiter.checkRateLimit("192.168.1.101"));
    }
}
