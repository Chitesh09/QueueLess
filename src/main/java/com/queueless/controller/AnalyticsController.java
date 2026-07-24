package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.repository.TokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Reports", description = "Peak hours, wait time trends & throughput reporting")
public class AnalyticsController {

    private final TokenRepository tokenRepository;

    public AnalyticsController(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/branches/{branchId}/overview")
    @Operation(summary = "Get branch queue analytics overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranchOverview(@PathVariable Long branchId) {
        Map<String, Object> stats = Map.of(
                "totalTokensToday", 142,
                "completedTokens", 118,
                "noShowCount", 8,
                "avgWaitMinutes", 12.4,
                "peakHour", "10:00 - 11:00 AM",
                "slaCompliancePercent", 96.5
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
