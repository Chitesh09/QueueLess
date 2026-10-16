package com.queueless.queue;

import com.queueless.common.constants.QueueConstants;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.dto.response.QueueEventResponse;
import com.queueless.dto.response.TokenResponse;
import com.queueless.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class QueueBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final TokenService tokenService;
    private final CounterRepository counterRepository;

    public QueueBroadcaster(SimpMessagingTemplate messagingTemplate,
                            TokenService tokenService,
                            CounterRepository counterRepository) {
        this.messagingTemplate = messagingTemplate;
        this.tokenService = tokenService;
        this.counterRepository = counterRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTokenCreated(TokenCreatedEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_CREATED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTokenCalled(TokenCalledEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_CALLED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTokenCheckedIn(TokenCheckedInEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_CHECKED_IN");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTokenCompleted(TokenCompletedEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_COMPLETED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTokenExpired(TokenExpiredEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_EXPIRED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTokenSkipped(TokenSkippedEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_SKIPPED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleCounterPaused(CounterPausedEvent event) {
        counterRepository.findById(event.getCounterId()).ifPresent(counter -> {
            broadcastCounterStatus(counter.getBranchId(), counter.getId(), "PAUSED");
        });
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleCounterResumed(CounterResumedEvent event) {
        counterRepository.findById(event.getCounterId()).ifPresent(counter -> {
            broadcastCounterStatus(counter.getBranchId(), counter.getId(), "ONLINE");
        });
    }

    public void broadcastCounterStatus(Long branchId, Long counterId, String status) {
        if (branchId == null) return;
        QueueEventResponse eventResponse = QueueEventResponse.builder()
                .eventType("COUNTER_STATUS_CHANGED")
                .branchId(branchId)
                .counterId(counterId)
                .counterStatus(status)
                .timestamp(Instant.now())
                .build();

        // Broadcast to branch counters channel
        messagingTemplate.convertAndSend("/topic/branch/" + branchId + "/counters", eventResponse);
        // Also broadcast to branch queue channel so display boards and operators catch the status change
        messagingTemplate.convertAndSend(QueueConstants.WS_TOPIC_BRANCH_QUEUE + branchId + "/queue", eventResponse);
    }

    private void broadcastTokenUpdate(com.queueless.domain.entity.Token token, String eventType) {
        if (token == null) return;
        TokenResponse privateResponse = tokenService.mapToTokenResponse(token);
        TokenResponse publicResponse = tokenService.mapToPublicTokenResponse(token);

        QueueEventResponse publicEventResponse = QueueEventResponse.builder()
                .eventType(eventType)
                .branchId(token.getBranchId())
                .serviceId(token.getServiceId())
                .counterId(token.getCounterId())
                .token(publicResponse)
                .timestamp(Instant.now())
                .build();

        // Topic 1: Branch queue channel for public display board & waiting room TV (sanitized public token)
        String branchTopic = QueueConstants.WS_TOPIC_BRANCH_QUEUE + token.getBranchId() + "/queue";
        messagingTemplate.convertAndSend(branchTopic, publicEventResponse);

        // Topic 2: Service queue channel for service-specific clients (sanitized public token)
        String serviceQueueTopic = QueueConstants.WS_TOPIC_QUEUE + token.getBranchId() + "/" + token.getServiceId();
        messagingTemplate.convertAndSend(serviceQueueTopic, publicEventResponse);

        // Topic 3: Private customer token channel (full private token response)
        String tokenTopic = QueueConstants.WS_TOPIC_TOKEN + token.getId();
        messagingTemplate.convertAndSend(tokenTopic, privateResponse);
    }
}
