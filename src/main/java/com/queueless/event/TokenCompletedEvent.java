package com.queueless.event;

import com.queueless.domain.entity.Token;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TokenCompletedEvent extends ApplicationEvent {
    private final Token token;

    public TokenCompletedEvent(Object source, Token token) {
        super(source);
        this.token = token;
    }
}
