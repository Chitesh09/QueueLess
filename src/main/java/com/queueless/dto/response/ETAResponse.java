package com.queueless.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ETAResponse {
    private Long serviceId;
    private Integer position;
    private Integer estimatedWaitMinutes;
    private Integer activeCounters;
    private Integer avgServiceDurationMinutes;
}
