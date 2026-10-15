package com.queueless.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEventResponse {
    private String eventType; // TOKEN_CREATED, TOKEN_CALLED, TOKEN_CHECKED_IN, TOKEN_COMPLETED, TOKEN_SKIPPED, TOKEN_EXPIRED, COUNTER_STATUS_CHANGED
    private Long branchId;
    private Long serviceId;
    private Long counterId;
    private TokenResponse token;
    private String counterStatus;
    private Instant timestamp;
}
