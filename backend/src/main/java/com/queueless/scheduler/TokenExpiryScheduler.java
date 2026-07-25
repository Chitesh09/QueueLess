package com.queueless.scheduler;

import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.event.TokenExpiredEvent;
import com.queueless.queue.QueueStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class TokenExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(TokenExpiryScheduler.class);

    private final TokenRepository tokenRepository;
    private final QueueStateMachine stateMachine;
    private final ApplicationEventPublisher eventPublisher;

    public TokenExpiryScheduler(TokenRepository tokenRepository,
                                QueueStateMachine stateMachine,
                                ApplicationEventPublisher eventPublisher) {
        this.tokenRepository = tokenRepository;
        this.stateMachine = stateMachine;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 30000) // Runs every 30 seconds
    @Transactional
    public void expireLapsedTokens() {
        Instant now = Instant.now();
        List<Token> lapsed = tokenRepository.findByStatusAndExpiresAtBefore("CALLED", now);

        for (Token token : lapsed) {
            log.info("Expiring lapsed token #{}", token.getTokenNumber());
            stateMachine.transition(token, "EXPIRED");
            tokenRepository.save(token);
            eventPublisher.publishEvent(new TokenExpiredEvent(this, token));
        }
    }
}
