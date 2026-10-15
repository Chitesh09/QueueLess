package com.queueless.queue;

import com.queueless.common.exception.BusinessException;
import com.queueless.domain.repository.TokenRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueueValidator {

    private final TokenRepository tokenRepository;

    private static final List<String> ACTIVE_STATUSES = List.of("CREATED", "WAITING", "CALLED", "CHECKED_IN", "IN_SERVICE");

    public QueueValidator(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void validateCanJoinQueue(Long userId, Long serviceId) {
        validateCanJoinQueue(userId, null, serviceId);
    }

    public void validateCanJoinQueue(Long userId, String guestSessionId, Long serviceId) {
        if (userId != null) {
            boolean exists = tokenRepository.existsByUserIdAndServiceIdAndStatusIn(userId, serviceId, ACTIVE_STATUSES);
            if (exists) {
                throw new BusinessException("User already has an active token in this queue");
            }
        } else if (guestSessionId != null && !guestSessionId.isBlank()) {
            boolean exists = tokenRepository.existsByGuestSessionIdAndServiceIdAndStatusIn(guestSessionId, serviceId, ACTIVE_STATUSES);
            if (exists) {
                throw new BusinessException("Guest already has an active token in this queue");
            }
        }
    }
}
