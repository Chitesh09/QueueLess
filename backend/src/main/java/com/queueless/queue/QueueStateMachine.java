package com.queueless.queue;

import com.queueless.common.exception.BusinessException;
import com.queueless.domain.entity.Token;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class QueueStateMachine {

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "CREATED", Set.of("WAITING", "CANCELLED"),
            "WAITING", Set.of("CALLED", "CANCELLED", "EXPIRED"),
            "CALLED", Set.of("CHECKED_IN", "SKIPPED", "EXPIRED"),
            "SKIPPED", Set.of("CALLED", "EXPIRED"),
            "CHECKED_IN", Set.of("IN_SERVICE", "COMPLETED"),
            "IN_SERVICE", Set.of("COMPLETED", "EXPIRED"),
            "COMPLETED", Set.of(),
            "CANCELLED", Set.of(),
            "EXPIRED", Set.of()
    );

    public void transition(Token token, String targetStatus) {
        String currentStatus = token.getStatus();
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new BusinessException(String.format(
                    "Invalid token state transition from %s to %s for token #%s",
                    currentStatus, targetStatus, token.getTokenNumber()));
        }
        token.setStatus(targetStatus);
    }
}
