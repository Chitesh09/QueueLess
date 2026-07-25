package com.queueless.queue;

import com.queueless.audit.annotation.Auditable;
import com.queueless.common.exception.BusinessException;
import com.queueless.common.exception.ResourceNotFoundException;
import com.queueless.domain.entity.*;
import com.queueless.domain.repository.*;
import com.queueless.dto.response.TokenResponse;
import com.queueless.event.*;
import com.queueless.security.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueEngine {

    private final TokenRepository tokenRepository;
    private final ServiceRepository serviceRepository;
    private final CounterRepository counterRepository;
    private final CounterServiceMappingRepository csmRepository;
    private final QueueValidator validator;
    private final QueueStateMachine stateMachine;
    private final PriorityScheduler scheduler;
    private final QRService qrService;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;

    public QueueEngine(TokenRepository tokenRepository,
                       ServiceRepository serviceRepository,
                       CounterRepository counterRepository,
                       CounterServiceMappingRepository csmRepository,
                       QueueValidator validator,
                       QueueStateMachine stateMachine,
                       PriorityScheduler scheduler,
                       QRService qrService,
                       TokenService tokenService,
                       ApplicationEventPublisher eventPublisher) {
        this.tokenRepository = tokenRepository;
        this.serviceRepository = serviceRepository;
        this.counterRepository = counterRepository;
        this.csmRepository = csmRepository;
        this.validator = validator;
        this.stateMachine = stateMachine;
        this.scheduler = scheduler;
        this.qrService = qrService;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Auditable(action = "JOIN_QUEUE", entityType = "Token")
    public TokenResponse joinQueue(Long userId, Long serviceId, String priorityClass) {
        validator.validateCanJoinQueue(userId, serviceId);

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

        long currentWaitingCount = tokenRepository.countByServiceIdAndStatus(serviceId, "WAITING");

        if (service.getDailyCapacity() != null && currentWaitingCount >= service.getDailyCapacity()) {
            throw new BusinessException("Daily capacity reached for this service");
        }

        String priority = (priorityClass != null && !priorityClass.isBlank()) ? priorityClass.toUpperCase() : "STANDARD";
        String tokenNumber = String.format("%s-%03d", service.getName().substring(0, Math.min(3, service.getName().length())).toUpperCase(), currentWaitingCount + 1);

        Token token = Token.builder()
                .organizationId(service.getOrganizationId())
                .branchId(service.getBranchId())
                .serviceId(serviceId)
                .userId(userId)
                .tokenNumber(tokenNumber)
                .status("WAITING")
                .priorityClass(priority)
                .queuePositionSnapshot((int) (currentWaitingCount + 1))
                .build();

        token = tokenRepository.save(token);
        String qrSig = qrService.generateQRSignature(token);
        token.setQrSignature(qrSig);
        token = tokenRepository.save(token);

        eventPublisher.publishEvent(new TokenCreatedEvent(this, token));
        return tokenService.mapToTokenResponse(token);
    }

    @Transactional
    @Auditable(action = "CALL_NEXT_TOKEN", entityType = "Token")
    public TokenResponse callNextToken(Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter", "id", counterId));

        if (!"ONLINE".equalsIgnoreCase(counter.getStatus())) {
            throw new BusinessException("Counter is currently offline or paused");
        }

        List<Long> mappedServiceIds = csmRepository.findByCounterId(counterId).stream()
                .map(CounterServiceMapping::getServiceId)
                .collect(Collectors.toList());

        if (mappedServiceIds.isEmpty()) {
            throw new BusinessException("No services mapped to this counter");
        }

        // Pessimistic Write Lock on waiting tokens for concurrency safety
        List<Token> waitingTokens = tokenRepository.findWaitingTokensForUpdate(mappedServiceIds);
        if (waitingTokens.isEmpty()) {
            return null; // Queue is empty
        }

        Token selectedToken = scheduler.selectNextToken(waitingTokens);
        stateMachine.transition(selectedToken, "CALLED");
        selectedToken.setCounterId(counterId);
        selectedToken.setCalledAt(Instant.now());
        selectedToken.setExpiresAt(Instant.now().plusSeconds(15 * 60)); // 15 min default grace period

        selectedToken = tokenRepository.save(selectedToken);
        eventPublisher.publishEvent(new TokenCalledEvent(this, selectedToken, counterId));

        return tokenService.mapToTokenResponse(selectedToken);
    }

    @Transactional
    @Auditable(action = "CHECK_IN_TOKEN", entityType = "Token")
    public TokenResponse checkInToken(Long tokenId, String qrSignature) {
        Token token = tokenService.getById(tokenId);
        if (!qrService.verifyQRSignature(token, qrSignature)) {
            throw new BusinessException("Invalid or forged QR token signature");
        }

        stateMachine.transition(token, "CHECKED_IN");
        token.setCheckedInAt(Instant.now());
        token = tokenRepository.save(token);

        eventPublisher.publishEvent(new TokenCheckedInEvent(this, token));
        return tokenService.mapToTokenResponse(token);
    }

    @Transactional
    @Auditable(action = "COMPLETE_SERVICE", entityType = "Token")
    public TokenResponse completeService(Long tokenId) {
        Token token = tokenService.getById(tokenId);
        stateMachine.transition(token, "COMPLETED");
        token.setCompletedAt(Instant.now());
        token = tokenRepository.save(token);

        eventPublisher.publishEvent(new TokenCompletedEvent(this, token));
        return tokenService.mapToTokenResponse(token);
    }

    @Transactional
    @Auditable(action = "SKIP_TOKEN", entityType = "Token")
    public TokenResponse skipToken(Long tokenId) {
        Token token = tokenService.getById(tokenId);
        stateMachine.transition(token, "SKIPPED");
        token = tokenRepository.save(token);

        eventPublisher.publishEvent(new TokenSkippedEvent(this, token));
        return tokenService.mapToTokenResponse(token);
    }
}
