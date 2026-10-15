package com.queueless.queue;

import com.queueless.common.exception.BusinessException;
import com.queueless.domain.entity.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueueStateMachineTest {

    private QueueStateMachine stateMachine;

    @BeforeEach
    public void setUp() {
        stateMachine = new QueueStateMachine();
    }

    @Test
    @DisplayName("Direct completion: WAITING -> CALLED -> COMPLETED (for services without QR check-in)")
    public void testDirectCompletionFromCalled() {
        Token token = Token.builder().tokenNumber("T-001").status("WAITING").build();

        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertEquals("CALLED", token.getStatus());

        assertDoesNotThrow(() -> stateMachine.transition(token, "COMPLETED"));
        assertEquals("COMPLETED", token.getStatus());
    }

    @Test
    @DisplayName("QR Check-in completion: WAITING -> CALLED -> CHECKED_IN -> COMPLETED")
    public void testCompletionWithCheckIn() {
        Token token = Token.builder().tokenNumber("T-002").status("WAITING").build();

        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertDoesNotThrow(() -> stateMachine.transition(token, "CHECKED_IN"));
        assertEquals("CHECKED_IN", token.getStatus());

        assertDoesNotThrow(() -> stateMachine.transition(token, "COMPLETED"));
        assertEquals("COMPLETED", token.getStatus());
    }

    @Test
    @DisplayName("In-Service flow: WAITING -> CALLED -> CHECKED_IN -> IN_SERVICE -> COMPLETED")
    public void testFullInServiceLifecycle() {
        Token token = Token.builder().tokenNumber("T-003").status("WAITING").build();

        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertDoesNotThrow(() -> stateMachine.transition(token, "CHECKED_IN"));
        assertDoesNotThrow(() -> stateMachine.transition(token, "IN_SERVICE"));
        assertEquals("IN_SERVICE", token.getStatus());

        assertDoesNotThrow(() -> stateMachine.transition(token, "COMPLETED"));
        assertEquals("COMPLETED", token.getStatus());
    }

    @Test
    @DisplayName("Skip and Recall flow: WAITING -> CALLED -> SKIPPED -> CALLED -> COMPLETED")
    public void testSkipAndRecallFlow() {
        Token token = Token.builder().tokenNumber("T-004").status("WAITING").build();

        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertDoesNotThrow(() -> stateMachine.transition(token, "SKIPPED"));
        assertEquals("SKIPPED", token.getStatus());

        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertEquals("CALLED", token.getStatus());

        assertDoesNotThrow(() -> stateMachine.transition(token, "COMPLETED"));
        assertEquals("COMPLETED", token.getStatus());
    }

    @Test
    @DisplayName("Cancellation flow: WAITING -> CANCELLED")
    public void testCancellationFlow() {
        Token token = Token.builder().tokenNumber("T-005").status("WAITING").build();

        assertDoesNotThrow(() -> stateMachine.transition(token, "CANCELLED"));
        assertEquals("CANCELLED", token.getStatus());
    }

    @Test
    @DisplayName("Expiry flow: WAITING -> CALLED -> EXPIRED")
    public void testExpiryFlow() {
        Token token = Token.builder().tokenNumber("T-006").status("WAITING").build();

        assertDoesNotThrow(() -> stateMachine.transition(token, "CALLED"));
        assertDoesNotThrow(() -> stateMachine.transition(token, "EXPIRED"));
        assertEquals("EXPIRED", token.getStatus());
    }

    @Test
    @DisplayName("Invalid transitions must throw BusinessException")
    public void testInvalidTransitionsThrowBusinessException() {
        Token waitingToken = Token.builder().tokenNumber("T-007").status("WAITING").build();
        assertThrows(BusinessException.class, () -> stateMachine.transition(waitingToken, "COMPLETED"));
        assertThrows(BusinessException.class, () -> stateMachine.transition(waitingToken, "CHECKED_IN"));

        Token completedToken = Token.builder().tokenNumber("T-008").status("COMPLETED").build();
        assertThrows(BusinessException.class, () -> stateMachine.transition(completedToken, "CALLED"));
        assertThrows(BusinessException.class, () -> stateMachine.transition(completedToken, "WAITING"));

        Token cancelledToken = Token.builder().tokenNumber("T-009").status("CANCELLED").build();
        assertThrows(BusinessException.class, () -> stateMachine.transition(cancelledToken, "WAITING"));

        Token expiredToken = Token.builder().tokenNumber("T-010").status("EXPIRED").build();
        assertThrows(BusinessException.class, () -> stateMachine.transition(expiredToken, "CALLED"));
    }
}
