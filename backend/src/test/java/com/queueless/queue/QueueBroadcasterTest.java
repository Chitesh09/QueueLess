package com.queueless.queue;

import com.queueless.common.constants.QueueConstants;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.dto.response.QueueEventResponse;
import com.queueless.dto.response.TokenResponse;
import com.queueless.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QueueBroadcasterTest {

    private SimpMessagingTemplate messagingTemplate;
    private TokenService tokenService;
    private CounterRepository counterRepository;
    private QueueBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        tokenService = mock(TokenService.class);
        counterRepository = mock(CounterRepository.class);
        broadcaster = new QueueBroadcaster(messagingTemplate, tokenService, counterRepository);
    }

    @Test
    @DisplayName("handleTokenCalled broadcasts to branch queue, service queue, and token topic")
    void handleTokenCalled_broadcastsToAllThreeDestinations() {
        Token token = Token.builder()
                .id(100L)
                .branchId(1L)
                .serviceId(2L)
                .counterId(5L)
                .tokenNumber("OPD-001")
                .status("CALLED")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .id(100L)
                .branchId(1L)
                .serviceId(2L)
                .counterId(5L)
                .tokenNumber("OPD-001")
                .status("CALLED")
                .build();

        when(tokenService.mapToTokenResponse(token)).thenReturn(tokenResponse);

        broadcaster.handleTokenCalled(new TokenCalledEvent(this, token, 5L));

        ArgumentCaptor<QueueEventResponse> branchCaptor = ArgumentCaptor.forClass(QueueEventResponse.class);
        verify(messagingTemplate).convertAndSend(eq(QueueConstants.WS_TOPIC_BRANCH_QUEUE + "1/queue"), branchCaptor.capture());

        QueueEventResponse branchEvent = branchCaptor.getValue();
        assertThat(branchEvent.getEventType()).isEqualTo("TOKEN_CALLED");
        assertThat(branchEvent.getBranchId()).isEqualTo(1L);
        assertThat(branchEvent.getCounterId()).isEqualTo(5L);
        assertThat(branchEvent.getToken().getTokenNumber()).isEqualTo("OPD-001");

        // Service topic
        verify(messagingTemplate).convertAndSend(eq(QueueConstants.WS_TOPIC_QUEUE + "1/2"), any(QueueEventResponse.class));

        // Individual customer ticket topic
        verify(messagingTemplate).convertAndSend(eq(QueueConstants.WS_TOPIC_TOKEN + "100"), eq(tokenResponse));
    }

    @Test
    @DisplayName("handleTokenCompleted broadcasts TOKEN_COMPLETED event")
    void handleTokenCompleted_broadcastsEvent() {
        Token token = Token.builder()
                .id(101L)
                .branchId(1L)
                .serviceId(2L)
                .tokenNumber("OPD-002")
                .status("COMPLETED")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .id(101L)
                .tokenNumber("OPD-002")
                .status("COMPLETED")
                .build();

        when(tokenService.mapToTokenResponse(token)).thenReturn(tokenResponse);

        broadcaster.handleTokenCompleted(new TokenCompletedEvent(this, token));

        ArgumentCaptor<QueueEventResponse> captor = ArgumentCaptor.forClass(QueueEventResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/branch/1/queue"), captor.capture());

        assertThat(captor.getValue().getEventType()).isEqualTo("TOKEN_COMPLETED");
        assertThat(captor.getValue().getToken().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("broadcastCounterStatus sends COUNTER_STATUS_CHANGED to counters and queue destinations")
    void broadcastCounterStatus_broadcastsToBranchCountersAndQueue() {
        broadcaster.broadcastCounterStatus(1L, 4L, "PAUSED");

        ArgumentCaptor<QueueEventResponse> captor = ArgumentCaptor.forClass(QueueEventResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/branch/1/counters"), captor.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/branch/1/queue"), any(QueueEventResponse.class));

        QueueEventResponse event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo("COUNTER_STATUS_CHANGED");
        assertThat(event.getCounterId()).isEqualTo(4L);
        assertThat(event.getCounterStatus()).isEqualTo("PAUSED");
    }

    @Test
    @DisplayName("handleCounterPaused event handler looks up counter and broadcasts")
    void handleCounterPaused_resolvesCounterAndBroadcasts() {
        Counter counter = Counter.builder()
                .id(3L)
                .branchId(2L)
                .name("Counter 3")
                .status("PAUSED")
                .build();

        when(counterRepository.findById(3L)).thenReturn(Optional.of(counter));

        broadcaster.handleCounterPaused(new CounterPausedEvent(this, 3L));

        verify(messagingTemplate).convertAndSend(eq("/topic/branch/2/counters"), any(QueueEventResponse.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/branch/2/queue"), any(QueueEventResponse.class));
    }
}
