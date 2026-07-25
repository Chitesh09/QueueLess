package com.queueless.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CounterResumedEvent extends ApplicationEvent {
    private final Long counterId;

    public CounterResumedEvent(Object source, Long counterId) {
        super(source);
        this.counterId = counterId;
    }
}
