package com.queueless.common.config;

import com.queueless.domain.entity.Branch;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.BranchRepository;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.security.CustomUserDetailsService;
import com.queueless.security.GuestSessionService;
import com.queueless.security.JwtTokenProvider;
import com.queueless.security.QueuelessUserPrincipal;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final GuestSessionService guestSessionService;
    private final TokenRepository tokenRepository;
    private final BranchRepository branchRepository;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider,
                           CustomUserDetailsService userDetailsService,
                           GuestSessionService guestSessionService,
                           TokenRepository tokenRepository,
                           BranchRepository branchRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.guestSessionService = guestSessionService;
        this.tokenRepository = tokenRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register SockJS endpoint
        registry.addEndpoint("/ws", "/ws-queue")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Register raw STOMP WebSocket endpoint
        registry.addEndpoint("/ws", "/ws-queue")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    handleConnect(accessor);
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    handleSubscribe(accessor);
                }

                return message;
            }
        });
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            if (!authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Malformed Authorization header in STOMP CONNECT");
            }
            String jwt = authHeader.substring(7).trim();
            if (!jwtTokenProvider.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid, expired, or malformed JWT token");
            }

            try {
                String email = jwtTokenProvider.getEmailFromToken(jwt);
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                Long organizationId = jwtTokenProvider.getOrganizationIdFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String role = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst().orElse("ROLE_USER");

                QueuelessUserPrincipal principal = new QueuelessUserPrincipal(
                        userId, organizationId, email, role, userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                accessor.setUser(authentication);

                if (accessor.getSessionAttributes() != null) {
                    accessor.getSessionAttributes().put("USER_ID", userId);
                    accessor.getSessionAttributes().put("ORG_ID", organizationId);
                    accessor.getSessionAttributes().put("ROLE", role);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to authenticate user from JWT: " + ex.getMessage(), ex);
            }
        }

        String guestTokenHeader = accessor.getFirstNativeHeader("X-Guest-Token");
        if (guestTokenHeader != null && !guestTokenHeader.isBlank()) {
            String guestId = guestSessionService.validateAndExtractGuestId(guestTokenHeader.trim());
            if (guestId == null) {
                throw new IllegalArgumentException("Invalid or expired guest session token in STOMP CONNECT");
            }
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("GUEST_ID", guestId);
            }
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("STOMP SUBSCRIBE destination is required");
        }

        // Validate top-level prefix
        if (!destination.startsWith("/topic/") && !destination.startsWith("/queue/") && !destination.startsWith("/user/")) {
            throw new IllegalArgumentException("Unauthorized WebSocket destination: " + destination);
        }

        QueuelessUserPrincipal authenticatedUser = extractUserPrincipal(accessor);
        String sessionGuestId = extractGuestId(accessor);

        // RULE 1: /user/** and /queue/** require authenticated user
        if (destination.startsWith("/user/") || destination.startsWith("/queue/")) {
            if (authenticatedUser == null) {
                throw new AccessDeniedException("Anonymous users cannot subscribe to private user or session queues: " + destination);
            }
        }

        // RULE 2: /topic/tokens/{tokenId} (Private customer ticket updates)
        if (destination.startsWith("/topic/tokens/")) {
            String tokenIdStr = destination.substring("/topic/tokens/".length());
            Long tokenId;
            try {
                tokenId = Long.parseLong(tokenIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token ID in destination: " + destination);
            }

            Token token = tokenRepository.findById(tokenId)
                    .orElseThrow(() -> new IllegalArgumentException("Token not found: " + tokenId));

            boolean authorized = false;

            // 2a. Authenticated customer who owns the token
            if (authenticatedUser != null && token.getUserId() != null && token.getUserId().equals(authenticatedUser.getUserId())) {
                authorized = true;
            }

            // 2b. Authenticated staff/operator/admin belonging to the same organization
            if (authenticatedUser != null && token.getOrganizationId() != null && token.getOrganizationId().equals(authenticatedUser.getOrganizationId())) {
                authorized = true;
            }

            // 2c. Super admin
            if (authenticatedUser != null && isSuperAdmin(authenticatedUser)) {
                authorized = true;
            }

            // 2d. Guest user who created the token with matching signed guestSessionId
            if (sessionGuestId != null && token.getGuestSessionId() != null && token.getGuestSessionId().equals(sessionGuestId)) {
                authorized = true;
            }

            if (!authorized) {
                throw new AccessDeniedException("Unauthorized subscription to token: " + tokenId);
            }
        }

        // RULE 3: /topic/branch/{branchId}/** (Branch queue and counter channels)
        if (destination.startsWith("/topic/branch/")) {
            String[] parts = destination.split("/");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Invalid branch destination format: " + destination);
            }
            Long branchId;
            try {
                branchId = Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid branch ID in destination: " + destination);
            }

            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));

            // Prevent authenticated users from subscribing across organizations
            if (authenticatedUser != null && !isSuperAdmin(authenticatedUser)) {
                if (authenticatedUser.getOrganizationId() != null && !authenticatedUser.getOrganizationId().equals(branch.getOrganizationId())) {
                    throw new AccessDeniedException("Cannot subscribe to another organization's branch queue: " + branchId);
                }
            }
        }

        // RULE 4: /topic/queue/{branchId}/{serviceId} (Service queue channel)
        if (destination.startsWith("/topic/queue/")) {
            String[] parts = destination.split("/");
            if (parts.length >= 4) {
                Long branchId;
                try {
                    branchId = Long.parseLong(parts[3]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid branch ID in service destination: " + destination);
                }

                Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));

                if (authenticatedUser != null && !isSuperAdmin(authenticatedUser)) {
                    if (authenticatedUser.getOrganizationId() != null && !authenticatedUser.getOrganizationId().equals(branch.getOrganizationId())) {
                        throw new AccessDeniedException("Cannot subscribe to another organization's service queue: " + destination);
                    }
                }
            }
        }
    }

    private QueuelessUserPrincipal extractUserPrincipal(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken auth &&
                auth.getPrincipal() instanceof QueuelessUserPrincipal qp) {
            return qp;
        }
        return null;
    }

    private String extractGuestId(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() != null && accessor.getSessionAttributes().containsKey("GUEST_ID")) {
            return (String) accessor.getSessionAttributes().get("GUEST_ID");
        }
        String subGuestToken = accessor.getFirstNativeHeader("X-Guest-Token");
        if (subGuestToken != null && !subGuestToken.isBlank()) {
            return guestSessionService.validateAndExtractGuestId(subGuestToken.trim());
        }
        return null;
    }

    private boolean isSuperAdmin(QueuelessUserPrincipal user) {
        return "ROLE_SUPER_ADMIN".equalsIgnoreCase(user.getRole()) ||
                "SUPER_ADMIN".equalsIgnoreCase(user.getRole()) ||
                user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"));
    }
}

