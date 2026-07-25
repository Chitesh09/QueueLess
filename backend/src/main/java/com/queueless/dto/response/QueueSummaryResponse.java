package com.queueless.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueSummaryResponse {
    private Long serviceId;
    private String serviceName;
    private Long totalWaiting;
    private Long totalInService;
    private Long totalCompletedToday;
    private Integer estimatedAvgWaitMinutes;
    private List<TokenResponse> waitingTokens;
    private TokenResponse currentCallingToken;
}
