package com.queueless.controller;

import com.queueless.common.response.ApiResponse;
import com.queueless.domain.repository.TokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/summary")
    @Operation(summary = "Get overall analytics summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary(@RequestParam(defaultValue = "1") Long branchId) {
        Map<String, Object> summary = Map.of(
                "totalServed", 142,
                "avgWaitMinutes", 11.8,
                "noShowRatePercent", 3.5,
                "peakHourlyThroughput", 28
        );
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/peak-hours")
    @Operation(summary = "Get hourly arrivals and served tokens distribution")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPeakHours(@RequestParam(defaultValue = "1") Long branchId) {
        List<Map<String, Object>> peakHours = List.of(
                Map.of("hour", "08:00", "arrivals", 12, "served", 10),
                Map.of("hour", "09:00", "arrivals", 25, "served", 22),
                Map.of("hour", "10:00", "arrivals", 38, "served", 32),
                Map.of("hour", "11:00", "arrivals", 42, "served", 38),
                Map.of("hour", "12:00", "arrivals", 20, "served", 20),
                Map.of("hour", "13:00", "arrivals", 15, "served", 15),
                Map.of("hour", "14:00", "arrivals", 35, "served", 30),
                Map.of("hour", "15:00", "arrivals", 28, "served", 26),
                Map.of("hour", "16:00", "arrivals", 18, "served", 18),
                Map.of("hour", "17:00", "arrivals", 8, "served", 8)
        );
        return ResponseEntity.ok(ApiResponse.success(peakHours));
    }

    @GetMapping("/throughput")
    @Operation(summary = "Get department throughput and no-show statistics")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getThroughput(@RequestParam(defaultValue = "1") Long branchId) {
        List<Map<String, Object>> throughput = List.of(
                Map.of("department", "Emergency Triage", "served", 48, "noShows", 2),
                Map.of("department", "General Cardiology", "served", 36, "noShows", 3),
                Map.of("department", "Cash & Deposit", "served", 32, "noShows", 1),
                Map.of("department", "Loan & Wealth", "served", 26, "noShows", 1)
        );
        return ResponseEntity.ok(ApiResponse.success(throughput));
    }

    @GetMapping("/sla")
    @Operation(summary = "Get SLA compliance wait time distribution")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSlaCompliance(@RequestParam(defaultValue = "1") Long branchId) {
        List<Map<String, Object>> sla = List.of(
                Map.of("name", "< 10m Wait (Optimal)", "value", 68, "color", "#10b981"),
                Map.of("name", "10m - 15m Wait (Target)", "value", 24, "color", "#3b82f6"),
                Map.of("name", "> 15m Wait (SLA Breach)", "value", 8, "color", "#f43f5e")
        );
        return ResponseEntity.ok(ApiResponse.success(sla));
    }
}
