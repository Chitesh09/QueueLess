package com.queueless.queue;

import com.queueless.common.exception.BusinessException;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.domain.repository.TokenSequenceRepository;
import com.queueless.dto.response.TokenResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class QueueEngineConcurrencyTest {

    @Autowired
    private QueueEngine queueEngine;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenSequenceRepository sequenceRepository;

    @Autowired
    private CounterRepository counterRepository;

    private static final Long TEST_SERVICE_ID = 1L; // Emergency Triage & OPD

    @BeforeEach
    public void setup() {
        tokenRepository.deleteAll();
        sequenceRepository.deleteAll();

        // Ensure Counter 1 and Counter 2 are ONLINE
        counterRepository.findById(1L).ifPresent(c -> {
            c.setStatus("ONLINE");
            counterRepository.save(c);
        });
        counterRepository.findById(2L).ifPresent(c -> {
            c.setStatus("ONLINE");
            counterRepository.save(c);
        });
    }

    @AfterEach
    public void tearDown() {
        tokenRepository.deleteAll();
        sequenceRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrent joins produce strictly unique monotonic token numbers without collisions")
    public void testConcurrentJoinsProduceUniqueMonotonicTokenNumbers() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<TokenResponse> createdTokens = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String guestId = "concurrent_guest_" + i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // ensure all threads fire concurrently
                    TokenResponse token = queueEngine.joinQueue(null, guestId, TEST_SERVICE_ID, "STANDARD");
                    createdTokens.add(token);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        try {
            startLatch.countDown();
            boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
            assertTrue(completed, "All join threads should finish within timeout");
            assertTrue(errors.isEmpty(), "Zero errors expected during concurrent joins, got: " + errors);
            assertEquals(threadCount, createdTokens.size(), "All 20 tokens must be successfully created");

            // Verify token number uniqueness
            Set<String> tokenNumbers = createdTokens.stream()
                    .map(TokenResponse::getTokenNumber)
                    .collect(Collectors.toSet());
            assertEquals(threadCount, tokenNumbers.size(), "Every token number must be strictly unique");

            // Verify monotonic numbering range from EME-001 to EME-020
            List<Integer> sequenceNumbers = createdTokens.stream()
                    .map(t -> Integer.parseInt(t.getTokenNumber().replace("EME-", "")))
                    .sorted()
                    .collect(Collectors.toList());

            for (int i = 0; i < threadCount; i++) {
                assertEquals(i + 1, sequenceNumbers.get(i), "Sequence numbers must be consecutive and monotonic");
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Completed tokens do not cause token number reuse")
    public void testCompletedTokensDoNotCauseNumberReuse() {
        // 1. Join queue for first customer
        TokenResponse first = queueEngine.joinQueue(null, "guest_1", TEST_SERVICE_ID, "STANDARD");
        assertEquals("EME-001", first.getTokenNumber());

        // 2. Counter 1 calls next token and completes it
        TokenResponse called = queueEngine.callNextToken(1L);
        assertNotNull(called);
        assertEquals(first.getId(), called.getId());

        TokenResponse completed = queueEngine.completeService(first.getId());
        assertEquals("COMPLETED", completed.getStatus());
        assertNotNull(completed.getCompletedAt());

        // 3. Second customer joins after first is completed
        TokenResponse second = queueEngine.joinQueue(null, "guest_2", TEST_SERVICE_ID, "STANDARD");
        assertEquals("EME-002", second.getTokenNumber(), "Second token must NOT reuse EME-001 despite waiting count decreasing");
    }

    @Test
    @DisplayName("Multiple simultaneous dispatches do not double-assign a token across counters")
    public void testConcurrentCounterDispatchesDoNotDoubleAssignToken() throws Exception {
        // Create 6 tokens
        for (int i = 1; i <= 6; i++) {
            queueEngine.joinQueue(null, "guest_dispatch_" + i, TEST_SERVICE_ID, "STANDARD");
        }

        int callers = 4;
        ExecutorService executor = Executors.newFixedThreadPool(callers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(callers);

        List<TokenResponse> calledTokens = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        try {
            // Counter 1 and Counter 2 both attempt to call next simultaneously
            for (int i = 0; i < callers; i++) {
                final Long counterId = (i % 2 == 0) ? 1L : 2L;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        TokenResponse token = queueEngine.callNextToken(counterId);
                        if (token != null) {
                            calledTokens.add(token);
                        }
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(10, TimeUnit.SECONDS);

            assertTrue(errors.isEmpty(), "Zero errors expected during dispatch, got: " + errors);
            assertFalse(calledTokens.isEmpty(), "Dispatches should have acquired tokens");

            // Verify that every called token has a unique ID (no double-dispatch)
            Set<Long> tokenIds = calledTokens.stream()
                    .map(TokenResponse::getId)
                    .collect(Collectors.toSet());
            assertEquals(calledTokens.size(), tokenIds.size(), "No token may be assigned to more than one counter");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Simultaneous dispatch of a single token allows exactly one counter to claim it")
    public void testSimultaneousDispatchOfSingleWaitingToken() throws Exception {
        // Only 1 token in the queue
        TokenResponse singleToken = queueEngine.joinQueue(null, "lone_guest", TEST_SERVICE_ID, "STANDARD");
        assertNotNull(singleToken);

        int competingCounters = 2;
        ExecutorService executor = Executors.newFixedThreadPool(competingCounters);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(competingCounters);

        List<TokenResponse> claimed = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        try {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TokenResponse t = queueEngine.callNextToken(1L);
                    if (t != null) claimed.add(t);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    startLatch.await();
                    TokenResponse t = queueEngine.callNextToken(2L);
                    if (t != null) claimed.add(t);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });

            startLatch.countDown();
            doneLatch.await(10, TimeUnit.SECONDS);

            assertTrue(errors.isEmpty(), "Zero errors expected during race for single token, got: " + errors);
            assertEquals(1, claimed.size(), "Exactly one counter must succeed in claiming the single waiting token");
            assertEquals(singleToken.getId(), claimed.get(0).getId());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Guest customer abuse prevention: same guest cannot have two active tokens in same queue")
    public void testGuestAbusePrevention() {
        TokenResponse token1 = queueEngine.joinQueue(null, "guest_abuse_check", TEST_SERVICE_ID, "STANDARD");
        assertNotNull(token1);

        // Attempting to join same queue with same guest session ID must throw BusinessException
        BusinessException ex = assertThrows(BusinessException.class, () ->
                queueEngine.joinQueue(null, "guest_abuse_check", TEST_SERVICE_ID, "STANDARD")
        );
        assertTrue(ex.getMessage().contains("already has an active token"));

        // A different guest can join without issues
        TokenResponse token2 = queueEngine.joinQueue(null, "different_guest", TEST_SERVICE_ID, "STANDARD");
        assertNotNull(token2);
    }

    @Test
    @DisplayName("Full token lifecycle: WAITING -> CALLED -> COMPLETED succeeds without exception")
    public void testFullTokenLifecycleWithDirectCompletion() {
        TokenResponse token = queueEngine.joinQueue(null, "lifecycle_guest", TEST_SERVICE_ID, "STANDARD");
        assertEquals("WAITING", token.getStatus());

        TokenResponse called = queueEngine.callNextToken(1L);
        assertEquals("CALLED", called.getStatus());
        assertEquals(1L, called.getCounterId());

        TokenResponse completed = queueEngine.completeService(token.getId());
        assertEquals("COMPLETED", completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }
}
