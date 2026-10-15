package com.queueless.security;

import com.queueless.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory sliding-window rate limiter for unauthenticated guest join operations.
 * Protects against queue hoarding and denial-of-service from clients repeatedly cycling guest sessions.
 */
@Component
public class GuestRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(GuestRateLimiter.class);

    // Limit unauthenticated guest joins to 10 requests per 60 seconds per IP address
    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> requestLog = new ConcurrentHashMap<>();

    /**
     * Checks whether the client IP has exceeded the rate limit for guest queue joins.
     * Throws BusinessException if rate limit is exceeded.
     */
    public void checkRateLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return;
        }

        String ip = clientIp.trim();
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(WINDOW_SECONDS);

        ConcurrentLinkedDeque<Instant> timestamps = requestLog.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        // Evict expired timestamps older than the rolling window
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
            log.warn("Rate limit exceeded for guest queue join from IP: {}", ip);
            throw new BusinessException("Rate limit exceeded for guest queue requests. Please wait a moment before trying again.");
        }

        timestamps.addLast(now);

        // Periodically purge stale IPs to prevent memory growth
        if (requestLog.size() > 5000) {
            cleanupStaleEntries(windowStart);
        }
    }

    private void cleanupStaleEntries(Instant windowStart) {
        requestLog.entrySet().removeIf(entry -> {
            ConcurrentLinkedDeque<Instant> deque = entry.getValue();
            while (!deque.isEmpty() && deque.peekFirst().isBefore(windowStart)) {
                deque.pollFirst();
            }
            return deque.isEmpty();
        });
    }

    public void resetForTesting() {
        requestLog.clear();
    }
}
