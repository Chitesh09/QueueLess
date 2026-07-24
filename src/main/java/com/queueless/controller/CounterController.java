package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.dto.response.TokenResponse;
import com.queueless.queue.QueueEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/counters")
@Tag(name = "Counter Operator Dashboard", description = "Call next token, skip, complete service & pause counter")
public class CounterController {

    private final QueueEngine queueEngine;
    private final CounterRepository counterRepository;

    public CounterController(QueueEngine queueEngine, CounterRepository counterRepository) {
        this.queueEngine = queueEngine;
        this.counterRepository = counterRepository;
    }

    @PostMapping("/{counterId}/call-next")
    @Operation(summary = "Call next waiting token based on Weighted Round-Robin fairness algorithm")
    public ResponseEntity<ApiResponse<TokenResponse>> callNext(@PathVariable Long counterId) {
        TokenResponse token = queueEngine.callNextToken(counterId);
        if (token == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No waiting tokens in queue"));
        }
        return ResponseEntity.ok(ApiResponse.success(token, "Token called successfully"));
    }

    @PostMapping("/{counterId}/complete/{tokenId}")
    @Operation(summary = "Mark customer service as completed")
    public ResponseEntity<ApiResponse<TokenResponse>> completeService(@PathVariable Long counterId,
                                                                       @PathVariable Long tokenId) {
        TokenResponse token = queueEngine.completeService(tokenId);
        return ResponseEntity.ok(ApiResponse.success(token, "Service marked as complete"));
    }

    @PostMapping("/{counterId}/skip/{tokenId}")
    @Operation(summary = "Skip absent customer token")
    public ResponseEntity<ApiResponse<TokenResponse>> skipToken(@PathVariable Long counterId,
                                                                 @PathVariable Long tokenId) {
        TokenResponse token = queueEngine.skipToken(tokenId);
        return ResponseEntity.ok(ApiResponse.success(token, "Token skipped"));
    }

    @PostMapping("/{counterId}/pause")
    @Operation(summary = "Pause counter operations")
    public ResponseEntity<ApiResponse<String>> pauseCounter(@PathVariable Long counterId) {
        Counter counter = counterRepository.findById(counterId).orElseThrow();
        counter.setStatus("PAUSED");
        counterRepository.save(counter);
        return ResponseEntity.ok(ApiResponse.success("PAUSED", "Counter paused"));
    }

    @PostMapping("/{counterId}/resume")
    @Operation(summary = "Resume counter operations")
    public ResponseEntity<ApiResponse<String>> resumeCounter(@PathVariable Long counterId) {
        Counter counter = counterRepository.findById(counterId).orElseThrow();
        counter.setStatus("ONLINE");
        counterRepository.save(counter);
        return ResponseEntity.ok(ApiResponse.success("ONLINE", "Counter resumed"));
    }
}
