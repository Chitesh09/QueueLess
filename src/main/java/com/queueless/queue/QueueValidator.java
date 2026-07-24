package com.queueless.queue;

import com.queueless.common.exception.BusinessException;
import com.queueless.domain.repository.TokenRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueueValidator {

    private final TokenRepository tokenRepository;

    public QueueValidator(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void validateCanJoinQueue(Long userId, Long serviceId) {
        List<String> activeStatuses = List.of("CREATED", "WAITING", "CALLED", "CHECKED_IN", "IN_SERVICE");
        boolean exists = tokenRepository.existsByUserIdAndServiceIdAndStatusIn(userId, serviceId, activeStatuses);
        if (exists) {
            throw new BusinessException("User already has an active token in this queue");
        }
    }
}
