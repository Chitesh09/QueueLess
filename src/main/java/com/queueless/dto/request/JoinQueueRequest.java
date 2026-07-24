package com.queueless.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinQueueRequest {
    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private String priorityClass; // EMERGENCY, SENIOR, APPOINTMENT, STANDARD
}
