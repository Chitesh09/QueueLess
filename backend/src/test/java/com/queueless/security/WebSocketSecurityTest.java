package com.queueless.security;

import com.queueless.common.config.WebSocketConfig;
import com.queueless.domain.entity.Branch;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.BranchRepository;
import com.queueless.domain.repository.TokenRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class WebSocketSecurityTest {

    private JwtTokenProvider jwtTokenProvider;
    private CustomUserDetailsService userDetailsService;
    private GuestSessionService guestSessionService;
    private TokenRepository tokenRepository;
    private BranchRepository branchRepository;
    private ChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        guestSessionService = mock(GuestSessionService.class);
        tokenRepository = mock(TokenRepository.class);
        branchRepository = mock(BranchRepository.class);

        WebSocketConfig config = new WebSocketConfig(
                jwtTokenProvider,
                userDetailsService,
                guestSessionService,
                tokenRepository,
                branchRepository
        );
        ChannelRegistration registration = mock(ChannelRegistration.class);
        ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);

        config.configureClientInboundChannel(registration);
        verify(registration).interceptors(captor.capture());
        interceptor = captor.getValue();
    }

    // ==========================================
    // STOMP CONNECT AUTHENTICATION TESTS
    // ==========================================

    @Test
    @DisplayName("CONNECT: Valid JWT sets authenticated QueuelessUserPrincipal")
    void connect_withValidJwt_authenticatesUser() {
        String validJwt = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(validJwt)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validJwt)).thenReturn("operator@hospital.com");
        when(jwtTokenProvider.getUserIdFromToken(validJwt)).thenReturn(42L);
        when(jwtTokenProvider.getOrganizationIdFromToken(validJwt)).thenReturn(10L);

        UserDetails userDetails = new User("operator@hospital.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        when(userDetailsService.loadUserByUsername("operator@hospital.com")).thenReturn(userDetails);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + validJwt);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("operator@hospital.com");

        QueuelessUserPrincipal principal = (QueuelessUserPrincipal) ((UsernamePasswordAuthenticationToken) resultAccessor.getUser()).getPrincipal();
        assertThat(principal.getUserId()).isEqualTo(42L);
        assertThat(principal.getOrganizationId()).isEqualTo(10L);
        assertThat(principal.getRole()).isEqualTo("ROLE_OPERATOR");
    }

    @Test
    @DisplayName("CONNECT: Invalid JWT throws IllegalArgumentException and rejects connection")
    void connect_withInvalidJwt_throwsException() {
        String invalidJwt = "bad.expired.token";
        when(jwtTokenProvider.validateToken(invalidJwt)).thenReturn(false);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + invalidJwt);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid, expired, or malformed JWT token");
    }

    @Test
    @DisplayName("CONNECT: Malformed Authorization header throws IllegalArgumentException")
    void connect_withMalformedAuthHeader_throwsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Basic user:pass");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Malformed Authorization header in STOMP CONNECT");
    }

    @Test
    @DisplayName("CONNECT: Valid X-Guest-Token establishes session guest ID")
    void connect_withValidGuestToken_setsSessionGuestId() {
        String guestToken = "guest_123.1800000.signature";
        when(guestSessionService.validateAndExtractGuestId(guestToken)).thenReturn("guest_123");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("X-Guest-Token", guestToken);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getSessionAttributes().get("GUEST_ID")).isEqualTo("guest_123");
    }

    @Test
    @DisplayName("CONNECT: Invalid X-Guest-Token throws IllegalArgumentException")
    void connect_withInvalidGuestToken_throwsException() {
        String forgedToken = "guest_123.fake.sig";
        when(guestSessionService.validateAndExtractGuestId(forgedToken)).thenReturn(null);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("X-Guest-Token", forgedToken);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired guest session token in STOMP CONNECT");
    }

    @Test
    @DisplayName("CONNECT: Anonymous connection without token allowed without authentication")
    void connect_anonymousWithoutToken_allowedAsAnonymous() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
    }

    // ==========================================
    // STOMP SUBSCRIBE AUTHORIZATION TESTS
    // ==========================================

    @Test
    @DisplayName("SUBSCRIBE: Anonymous user cannot subscribe to /user/** or /queue/**")
    void subscribe_anonymousToUserOrQueue_denied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/queue/token-updates");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Anonymous users cannot subscribe to private user or session queues");
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/tokens/{tokenId} allowed for customer owner")
    void subscribe_tokenTopic_allowedForTokenOwner() {
        Token token = Token.builder().id(50L).userId(101L).organizationId(1L).build();
        when(tokenRepository.findById(50L)).thenReturn(Optional.of(token));

        QueuelessUserPrincipal principal = new QueuelessUserPrincipal(
                101L, 1L, "customer@test.com", "ROLE_USER", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/tokens/50");
        accessor.setUser(auth);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/tokens/{tokenId} denied for authenticated user who does not own the token")
    void subscribe_tokenTopic_deniedForDifferentUser() {
        Token token = Token.builder().id(50L).userId(101L).organizationId(1L).build();
        when(tokenRepository.findById(50L)).thenReturn(Optional.of(token));

        // Different user in a different organization
        QueuelessUserPrincipal otherUser = new QueuelessUserPrincipal(
                999L, 2L, "attacker@test.com", "ROLE_USER", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(otherUser, null, otherUser.getAuthorities());

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/tokens/50");
        accessor.setUser(auth);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Unauthorized subscription to token: 50");
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/tokens/{tokenId} allowed for staff operator belonging to same organization")
    void subscribe_tokenTopic_allowedForOrgStaff() {
        Token token = Token.builder().id(50L).userId(101L).organizationId(1L).build();
        when(tokenRepository.findById(50L)).thenReturn(Optional.of(token));

        QueuelessUserPrincipal operator = new QueuelessUserPrincipal(
                202L, 1L, "operator@test.com", "ROLE_OPERATOR", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(operator, null, operator.getAuthorities());

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/tokens/50");
        accessor.setUser(auth);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/tokens/{tokenId} allowed for guest with matching signed guestSessionId")
    void subscribe_tokenTopic_allowedForGuestOwner() {
        Token guestToken = Token.builder().id(60L).guestSessionId("guest_abc123").organizationId(1L).build();
        when(tokenRepository.findById(60L)).thenReturn(Optional.of(guestToken));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/tokens/60");
        accessor.setSessionAttributes(new HashMap<>());
        accessor.getSessionAttributes().put("GUEST_ID", "guest_abc123");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/tokens/{tokenId} denied for anonymous without guest token")
    void subscribe_tokenTopic_deniedForAnonymousWithoutGuestToken() {
        Token guestToken = Token.builder().id(60L).guestSessionId("guest_abc123").organizationId(1L).build();
        when(tokenRepository.findById(60L)).thenReturn(Optional.of(guestToken));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/tokens/60");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Unauthorized subscription to token: 60");
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/branch/{branchId}/queue denied for authenticated user from another organization")
    void subscribe_branchQueue_deniedForCrossOrgAuthenticatedUser() {
        Branch branch = Branch.builder().id(1L).organizationId(1L).name("Branch 1").build();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        QueuelessUserPrincipal crossOrgUser = new QueuelessUserPrincipal(
                303L, 2L, "staff@otherorg.com", "ROLE_OPERATOR", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(crossOrgUser, null, crossOrgUser.getAuthorities());

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/branch/1/queue");
        accessor.setUser(auth);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cannot subscribe to another organization's branch queue: 1");
    }

    @Test
    @DisplayName("SUBSCRIBE: /topic/branch/{branchId}/queue allowed for public/anonymous visitor")
    void subscribe_branchQueue_allowedForPublic() {
        Branch branch = Branch.builder().id(1L).organizationId(1L).name("Branch 1").build();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/branch/1/queue");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE: Unauthorized destination prefix throws IllegalArgumentException")
    void subscribe_unauthorizedPrefix_throwsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/internal/admin/secrets");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unauthorized WebSocket destination: /internal/admin/secrets");
    }
}
