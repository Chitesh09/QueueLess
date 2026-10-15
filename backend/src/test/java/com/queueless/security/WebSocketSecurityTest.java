package com.queueless.security;

import com.queueless.common.config.WebSocketConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class WebSocketSecurityTest {

    private JwtTokenProvider jwtTokenProvider;
    private CustomUserDetailsService userDetailsService;
    private ChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userDetailsService = mock(CustomUserDetailsService.class);

        WebSocketConfig config = new WebSocketConfig(jwtTokenProvider, userDetailsService);
        ChannelRegistration registration = mock(ChannelRegistration.class);
        ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);

        config.configureClientInboundChannel(registration);
        verify(registration).interceptors(captor.capture());
        interceptor = captor.getValue();
    }

    @Test
    @DisplayName("CONNECT frame with valid JWT sets authenticated user in StompHeaderAccessor")
    void connect_withValidJwt_authenticatesUser() {
        String validJwt = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(validJwt)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validJwt)).thenReturn("operator@hospital.com");

        UserDetails userDetails = new User("operator@hospital.com", "pass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("operator@hospital.com")).thenReturn(userDetails);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + validJwt);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("operator@hospital.com");
    }

    @Test
    @DisplayName("CONNECT frame without JWT connects as guest/unauthenticated without error")
    void connect_withoutJwt_allowsAnonymousConnection() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
    }

    @Test
    @DisplayName("SUBSCRIBE frame with valid destination prefix is permitted")
    void subscribe_withValidTopicPrefix_allowed() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/branch/1/queue");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE frame with unauthorized destination prefix throws IllegalArgumentException")
    void subscribe_withUnauthorizedPrefix_throwsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/malicious/internal/topic");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unauthorized WebSocket destination");
    }
}
