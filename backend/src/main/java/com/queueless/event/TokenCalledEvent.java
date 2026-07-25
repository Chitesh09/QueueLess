package com.queueless.event;

import com.queueless.domain.entity.Token;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TokenCalledEvent extends ApplicationEvent {
    private final Token token;
    private final Long counterId;

    public TokenCalledEvent(Object source, Token token, Long counterId) {
        super(source);
        this.token = token;
        this.counterId = counterId;
    }
}
