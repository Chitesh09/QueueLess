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
    @EventListener
    public void handleTokenCreated(TokenCreatedEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_CREATED");
    }

    @Async
    @EventListener
    public void handleTokenCalled(TokenCalledEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_CALLED");
    }

    @Async
    @EventListener
    public void handleTokenCheckedIn(TokenCheckedInEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_CHECKED_IN");
    }

    @Async
    @EventListener
    public void handleTokenCompleted(TokenCompletedEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_COMPLETED");
    }

    @Async
    @EventListener
    public void handleTokenExpired(TokenExpiredEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_EXPIRED");
    }

    @Async
    @EventListener
    public void handleTokenSkipped(TokenSkippedEvent event) {
        broadcastTokenUpdate(event.getToken(), "TOKEN_SKIPPED");
    }

    @Async
    @EventListener
    public void handleCounterPaused(CounterPausedEvent event) {
        counterRepository.findById(event.getCounterId()).ifPresent(counter -> {
            broadcastCounterStatus(counter.getBranchId(), counter.getId(), "PAUSED");
        });
    }

    @Async
    @EventListener
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
        TokenResponse response = tokenService.mapToTokenResponse(token);

        QueueEventResponse eventResponse = QueueEventResponse.builder()
                .eventType(eventType)
                .branchId(token.getBranchId())
                .serviceId(token.getServiceId())
                .counterId(token.getCounterId())
                .token(response)
                .timestamp(Instant.now())
                .build();

        // Topic 1: Branch queue channel for display board & operator dashboards
        String branchTopic = QueueConstants.WS_TOPIC_BRANCH_QUEUE + token.getBranchId() + "/queue";
        messagingTemplate.convertAndSend(branchTopic, eventResponse);

        // Topic 2: Service queue channel for service-specific clients
        String serviceQueueTopic = QueueConstants.WS_TOPIC_QUEUE + token.getBranchId() + "/" + token.getServiceId();
        messagingTemplate.convertAndSend(serviceQueueTopic, eventResponse);

        // Topic 3: Individual token user channel for customer live ticket view
        String tokenTopic = QueueConstants.WS_TOPIC_TOKEN + token.getId();
        messagingTemplate.convertAndSend(tokenTopic, response);
    }
}
