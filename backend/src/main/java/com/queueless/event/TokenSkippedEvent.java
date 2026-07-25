package com.queueless.event;

import com.queueless.domain.entity.Token;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TokenSkippedEvent extends ApplicationEvent {
    private final Token token;

    public TokenSkippedEvent(Object source, Token token) {
        super(source);
        this.token = token;
    }
}
