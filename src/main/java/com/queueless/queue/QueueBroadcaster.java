package com.queueless.queue;

import com.queueless.common.constants.QueueConstants;
import com.queueless.dto.response.TokenResponse;
import com.queueless.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class QueueBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final TokenService tokenService;

    public QueueBroadcaster(SimpMessagingTemplate messagingTemplate, TokenService tokenService) {
        this.messagingTemplate = messagingTemplate;
        this.tokenService = tokenService;
    }

    @Async
    @EventListener
    public void handleTokenCreated(TokenCreatedEvent event) {
        broadcastTokenUpdate(event.getToken());
    }

    @Async
    @EventListener
    public void handleTokenCalled(TokenCalledEvent event) {
        broadcastTokenUpdate(event.getToken());
    }

    @Async
    @EventListener
    public void handleTokenCheckedIn(TokenCheckedInEvent event) {
        broadcastTokenUpdate(event.getToken());
    }

    @Async
    @EventListener
    public void handleTokenCompleted(TokenCompletedEvent event) {
        broadcastTokenUpdate(event.getToken());
    }

    @Async
    @EventListener
    public void handleTokenExpired(TokenExpiredEvent event) {
        broadcastTokenUpdate(event.getToken());
    }

    @Async
    @EventListener
    public void handleTokenSkipped(TokenSkippedEvent event) {
        broadcastTokenUpdate(event.getToken());
    }

    private void broadcastTokenUpdate(com.queueless.domain.entity.Token token) {
        if (token == null) return;
        TokenResponse response = tokenService.mapToTokenResponse(token);

        // Topic 1: Service queue channel for all clients listening to branch/service
        String queueTopic = QueueConstants.WS_TOPIC_QUEUE + token.getBranchId() + "/" + token.getServiceId();
        messagingTemplate.convertAndSend(queueTopic, response);

        // Topic 2: Individual token user channel
        String tokenTopic = QueueConstants.WS_TOPIC_TOKEN + token.getId();
        messagingTemplate.convertAndSend(tokenTopic, response);
    }
}
