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
public class TokenResponse {
    private Long id;
    private Long organizationId;
    private Long branchId;
    private Long serviceId;
    private String serviceName;
    private Long userId;
    private String userName;
    private Long counterId;
    private String counterName;
    private String tokenNumber;
    private String status;
    private String priorityClass;
    private Integer queuePosition;
    private Integer estimatedWaitMinutes;
    private String qrSignature;
    private Instant calledAt;
    private Instant checkedInAt;
    private Instant completedAt;
    private Instant expiresAt;
    private Instant createdAt;
}
