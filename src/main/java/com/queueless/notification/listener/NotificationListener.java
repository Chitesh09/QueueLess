package com.queueless.notification.listener;

import com.queueless.domain.entity.Token;
import com.queueless.event.*;
import com.queueless.notification.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final EmailNotificationService emailService;

    public NotificationListener(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void onTokenCreated(TokenCreatedEvent event) {
        Token token = event.getToken();
        log.info("Notification Listener: Token #{} created for User ID {}", token.getTokenNumber(), token.getUserId());
    }

    @Async
    @EventListener
    public void onTokenCalled(TokenCalledEvent event) {
        Token token = event.getToken();
        log.info("Notification Listener: Token #{} CALLED at Counter ID {}", token.getTokenNumber(), event.getCounterId());
        emailService.sendEmail("customer@queueless.com", "Your turn is ready!", "Your token " + token.getTokenNumber() + " was called.");
    }

    @Async
    @EventListener
    public void onTokenExpired(TokenExpiredEvent event) {
        Token token = event.getToken();
        log.info("Notification Listener: Token #{} EXPIRED", token.getTokenNumber());
    }
}
