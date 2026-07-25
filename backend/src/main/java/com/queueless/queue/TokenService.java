package com.queueless.queue;

import com.queueless.common.exception.ResourceNotFoundException;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.entity.ServiceEntity;
import com.queueless.domain.entity.Token;
import com.queueless.domain.entity.User;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.domain.repository.ServiceRepository;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.domain.repository.UserRepository;
import com.queueless.dto.response.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final ServiceRepository serviceRepository;
    private final CounterRepository counterRepository;
    private final UserRepository userRepository;
    private final ETAEngine etaEngine;

    public TokenService(TokenRepository tokenRepository,
                        ServiceRepository serviceRepository,
                        CounterRepository counterRepository,
                        UserRepository userRepository,
                        ETAEngine etaEngine) {
        this.tokenRepository = tokenRepository;
        this.serviceRepository = serviceRepository;
        this.counterRepository = counterRepository;
        this.userRepository = userRepository;
        this.etaEngine = etaEngine;
    }

    public TokenResponse mapToTokenResponse(Token token) {
        if (token == null) return null;

        String serviceName = serviceRepository.findById(token.getServiceId())
                .map(ServiceEntity::getName).orElse("Unknown Service");

        String counterName = token.getCounterId() != null ?
                counterRepository.findById(token.getCounterId()).map(Counter::getName).orElse(null) : null;

        String userName = userRepository.findById(token.getUserId())
                .map(User::getName).orElse("Customer");

        Integer etaMinutes = 0;
        if ("WAITING".equals(token.getStatus())) {
            etaMinutes = etaEngine.calculateETA(token.getServiceId(), token.getQueuePositionSnapshot()).getEstimatedWaitMinutes();
        }

        return TokenResponse.builder()
                .id(token.getId())
                .organizationId(token.getOrganizationId())
                .branchId(token.getBranchId())
                .serviceId(token.getServiceId())
                .serviceName(serviceName)
                .userId(token.getUserId())
                .userName(userName)
                .counterId(token.getCounterId())
                .counterName(counterName)
                .tokenNumber(token.getTokenNumber())
                .status(token.getStatus())
                .priorityClass(token.getPriorityClass())
                .queuePosition(token.getQueuePositionSnapshot())
                .estimatedWaitMinutes(etaMinutes)
                .qrSignature(token.getQrSignature())
                .calledAt(token.getCalledAt())
                .checkedInAt(token.getCheckedInAt())
                .completedAt(token.getCompletedAt())
                .expiresAt(token.getExpiresAt())
                .createdAt(token.getCreatedAt())
                .build();
    }

    public Token getById(Long tokenId) {
        return tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "id", tokenId));
    }
}
