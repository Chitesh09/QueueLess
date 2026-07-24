package com.queueless.queue;

import com.queueless.domain.entity.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class PriorityScheduler {

    @Value("${queueless.queue.max-starvation-threshold-sec:600}")
    private long maxStarvationThresholdSec;

    private static final Map<String, Integer> PRIORITY_WEIGHTS = Map.of(
            "EMERGENCY", 100,
            "SENIOR", 40,
            "APPOINTMENT", 20,
            "STANDARD", 10
    );

    public Token selectNextToken(List<Token> waitingTokens) {
        if (waitingTokens == null || waitingTokens.isEmpty()) {
            return null;
        }

        Instant now = Instant.now();

        return waitingTokens.stream()
                .max(Comparator.comparingDouble(token -> calculatePriorityScore(token, now)))
                .orElse(waitingTokens.get(0));
    }

    public double calculatePriorityScore(Token token, Instant now) {
        int baseWeight = PRIORITY_WEIGHTS.getOrDefault(token.getPriorityClass(), 10);
        long waitTimeSec = Math.max(0, Duration.between(token.getCreatedAt(), now).getSeconds());

        // Starvation multiplier grows exponentially as waitTime approaches threshold
        double starvationFactor = Math.pow(1.0 + ((double) waitTimeSec / maxStarvationThresholdSec), 1.5);

        return baseWeight * starvationFactor;
    }
}
