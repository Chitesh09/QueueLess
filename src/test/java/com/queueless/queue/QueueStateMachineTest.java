package com.queueless.queue;

import com.queueless.common.exception.BusinessException;
import com.queueless.domain.entity.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueueStateMachineTest {

    private QueueStateMachine stateMachine;

    @BeforeEach
    public void setUp() {
        stateMachine = new QueueStateMachine();
    }

    @Test
    public void testValidTransitions() {
        Token token = Token.builder().tokenNumber("T-001").status("WAITING").build();

        // WAITING -> CALLED
        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertEquals("CALLED", token.getStatus());

        // CALLED -> CHECKED_IN
        assertDoesNotThrow(() -> stateMachine.transition(token, "CHECKED_IN"));
        assertEquals("CHECKED_IN", token.getStatus());

        // CHECKED_IN -> COMPLETED
        assertDoesNotThrow(() -> stateMachine.transition(token, "COMPLETED"));
        assertEquals("COMPLETED", token.getStatus());
    }

    @Test
    public void testInvalidTransitionThrowsException() {
        Token token = Token.builder().tokenNumber("T-001").status("WAITING").build();

        // WAITING -> COMPLETED is illegal
        assertThrows(BusinessException.class, () -> stateMachine.transition(token, "COMPLETED"));
    }
}
