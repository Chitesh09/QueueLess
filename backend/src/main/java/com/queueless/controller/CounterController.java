package com.queueless.controller;

import com.queueless.common.exception.ResourceNotFoundException;
import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.entity.CounterServiceMapping;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.domain.repository.CounterServiceMappingRepository;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.dto.response.TokenResponse;
import com.queueless.queue.QueueBroadcaster;
import com.queueless.queue.QueueEngine;
import com.queueless.queue.TokenService;
import com.queueless.event.CounterPausedEvent;
import com.queueless.event.CounterResumedEvent;
import com.queueless.security.QueuelessUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/counters")
@Tag(name = "Counter Operator Dashboard", description = "Call next token, skip, complete service & pause counter")
public class CounterController {

    private final QueueEngine queueEngine;
    private final CounterRepository counterRepository;
    private final CounterServiceMappingRepository csmRepository;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final QueueBroadcaster queueBroadcaster;
    private final ApplicationEventPublisher eventPublisher;

    public CounterController(QueueEngine queueEngine,
                             CounterRepository counterRepository,
                             CounterServiceMappingRepository csmRepository,
                             TokenRepository tokenRepository,
                             TokenService tokenService,
                             QueueBroadcaster queueBroadcaster,
                             ApplicationEventPublisher eventPublisher) {
        this.queueEngine = queueEngine;
        this.counterRepository = counterRepository;
        this.csmRepository = csmRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.queueBroadcaster = queueBroadcaster;
        this.eventPublisher = eventPublisher;
    }

    private Counter validateCounterAccess(Long counterId, QueuelessUserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated access to counter operations");
        }
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter", "id", counterId));

        if (!principal.isSuperAdmin() && !counter.getOrganizationId().equals(principal.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Counter does not belong to user's organization");
        }
        return counter;
    }

    private void validateTokenAccess(Long tokenId, QueuelessUserPrincipal principal) {
        Token token = tokenService.getById(tokenId);
        if (!principal.isSuperAdmin() && !token.getOrganizationId().equals(principal.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Token does not belong to user's organization");
        }
    }

    @GetMapping("/{counterId}/queue")
    @Operation(summary = "Get list of waiting tokens assigned to this counter's services")
    public ResponseEntity<ApiResponse<List<TokenResponse>>> getCounterQueue(@PathVariable Long counterId,
                                                                            @AuthenticationPrincipal QueuelessUserPrincipal principal) {
        validateCounterAccess(counterId, principal);

        List<Long> mappedServiceIds = csmRepository.findByCounterId(counterId).stream()
                .map(CounterServiceMapping::getServiceId)
                .collect(Collectors.toList());

        if (mappedServiceIds.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        List<Token> waitingTokens = tokenRepository.findByServiceIdInAndStatusOrderByCreatedAtAsc(mappedServiceIds, "WAITING");
        List<TokenResponse> responseList = waitingTokens.stream()
                .map(tokenService::mapToTokenResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    @PostMapping("/{counterId}/call-next")
    @Operation(summary = "Call next waiting token based on Weighted Round-Robin fairness algorithm")
    public ResponseEntity<ApiResponse<TokenResponse>> callNext(@PathVariable Long counterId,
                                                                @AuthenticationPrincipal QueuelessUserPrincipal principal) {
        validateCounterAccess(counterId, principal);

        TokenResponse token = queueEngine.callNextToken(counterId);
        if (token == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No waiting tokens in queue"));
        }
        return ResponseEntity.ok(ApiResponse.success(token, "Token called successfully"));
    }

    @PostMapping("/{counterId}/complete/{tokenId}")
    @Operation(summary = "Mark customer service as completed")
    public ResponseEntity<ApiResponse<TokenResponse>> completeService(@PathVariable Long counterId,
                                                                       @PathVariable Long tokenId,
                                                                       @AuthenticationPrincipal QueuelessUserPrincipal principal) {
        validateCounterAccess(counterId, principal);
        validateTokenAccess(tokenId, principal);

        TokenResponse token = queueEngine.completeService(tokenId);
        return ResponseEntity.ok(ApiResponse.success(token, "Service marked as complete"));
    }

    @PostMapping("/{counterId}/skip/{tokenId}")
    @Operation(summary = "Skip absent customer token")
    public ResponseEntity<ApiResponse<TokenResponse>> skipToken(@PathVariable Long counterId,
                                                                 @PathVariable Long tokenId,
                                                                 @AuthenticationPrincipal QueuelessUserPrincipal principal) {
        validateCounterAccess(counterId, principal);
        validateTokenAccess(tokenId, principal);

        TokenResponse token = queueEngine.skipToken(tokenId);
        return ResponseEntity.ok(ApiResponse.success(token, "Token skipped"));
    }

    @PostMapping("/{counterId}/pause")
    @Operation(summary = "Pause counter operations")
    @Transactional
    public ResponseEntity<ApiResponse<String>> pauseCounter(@PathVariable Long counterId,
                                                            @AuthenticationPrincipal QueuelessUserPrincipal principal) {
        Counter counter = validateCounterAccess(counterId, principal);
        counter.setStatus("PAUSED");
        counterRepository.save(counter);
        eventPublisher.publishEvent(new CounterPausedEvent(this, counterId));
        return ResponseEntity.ok(ApiResponse.success("PAUSED", "Counter paused"));
    }

    @PostMapping("/{counterId}/resume")
    @Operation(summary = "Resume counter operations")
    @Transactional
    public ResponseEntity<ApiResponse<String>> resumeCounter(@PathVariable Long counterId,
                                                             @AuthenticationPrincipal QueuelessUserPrincipal principal) {
        Counter counter = validateCounterAccess(counterId, principal);
        counter.setStatus("ONLINE");
        counterRepository.save(counter);
        eventPublisher.publishEvent(new CounterResumedEvent(this, counterId));
        return ResponseEntity.ok(ApiResponse.success("ONLINE", "Counter resumed"));
    }
}
