package com.queueless.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class GuestSessionService {

    private static final Logger log = LoggerFactory.getLogger(GuestSessionService.class);
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long DEFAULT_EXPIRY_MS = 24 * 60 * 60 * 1000L; // 24 hours

    @Value("${queueless.guest.secret:${queueless.jwt.secret:default-secret-key-for-queue-less-guest-sessions-2026}}")
    private String secret;

    public record GuestSession(String guestId, String signedToken, boolean isNew) {}

    /**
     * Resolves an authenticated guest identity from a cryptographically signed guest token.
     * If the incoming token is missing, expired, tampered, or forged, a fresh secure guest session is created.
     * Prevents arbitrary guest identity spoofing and impersonation.
     */
    public GuestSession resolveOrCreateGuestSession(String incomingToken) {
        if (incomingToken != null && !incomingToken.isBlank()) {
            String guestId = validateAndExtractGuestId(incomingToken.trim());
            if (guestId != null) {
                return new GuestSession(guestId, incomingToken.trim(), false);
            }
            log.warn("Invalid, expired, or forged guest token received. Issuing a new cryptographic session.");
        }
        return createNewGuestSession();
    }

    /**
     * Resolves token candidate from either X-Guest-Token header, legacy header, or HTTP cookie.
     */
    public String extractTokenCandidate(String guestTokenHeader, String legacyHeader, jakarta.servlet.http.Cookie[] cookies) {
        if (guestTokenHeader != null && !guestTokenHeader.isBlank()) {
            return guestTokenHeader.trim();
        }
        if (legacyHeader != null && !legacyHeader.isBlank() && legacyHeader.contains(".")) {
            return legacyHeader.trim();
        }
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("queueless_guest_token".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue().trim();
                }
            }
        }
        return null;
    }

    /**
     * Creates a new cryptographically signed guest session.
     */
    public GuestSession createNewGuestSession() {
        String guestId = "guest_" + UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + DEFAULT_EXPIRY_MS;
        String signature = computeSignature(guestId, expiry);
        String signedToken = guestId + "." + expiry + "." + signature;
        return new GuestSession(guestId, signedToken, true);
    }

    /**
     * Validates a guest token signature and expiration, returning the guestId if valid, or null if invalid.
     */
    public String validateAndExtractGuestId(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        String guestId = parts[0];
        String expiryStr = parts[1];
        String providedSignature = parts[2];

        try {
            long expiry = Long.parseLong(expiryStr);
            if (System.currentTimeMillis() > expiry) {
                log.debug("Guest token expired at {}", expiry);
                return null;
            }

            String expectedSignature = computeSignature(guestId, expiry);
            if (constantTimeEquals(expectedSignature, providedSignature)) {
                return guestId;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }

    /**
     * Helper to sign a specific guestId and expiry for testing or verification.
     */
    public String createSignedTokenForGuest(String guestId, long expiryMs) {
        long expiry = System.currentTimeMillis() + expiryMs;
        String signature = computeSignature(guestId, expiry);
        return guestId + "." + expiry + "." + signature;
    }

    private String computeSignature(String guestId, long expiry) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(secretKey);
            String payload = guestId + ":" + expiry;
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate HMAC for guest token", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(aBytes, bBytes);
    }
}
