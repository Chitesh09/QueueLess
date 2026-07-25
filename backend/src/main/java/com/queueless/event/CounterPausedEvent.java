package com.queueless.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CounterPausedEvent extends ApplicationEvent {
    private final Long counterId;

    public CounterPausedEvent(Object source, Long counterId) {
        super(source);
        this.counterId = counterId;
    }
}
