package com.queueless.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "counter_id")
    private Long counterId;

    @Column(name = "token_number", nullable = false)
    private String tokenNumber;

    @Column(nullable = false)
    private String status; // CREATED, WAITING, CALLED, CHECKED_IN, IN_SERVICE, COMPLETED, EXPIRED, SKIPPED, CANCELLED

    @Column(name = "priority_class", nullable = false)
    private String priorityClass; // EMERGENCY, SENIOR, APPOINTMENT, STANDARD

    @Column(name = "queue_position_snapshot", nullable = false)
    private Integer queuePositionSnapshot;

    @Column(name = "called_at")
    private Instant calledAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "qr_signature", length = 512)
    private String qrSignature;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) status = "WAITING";
        if (priorityClass == null) priorityClass = "STANDARD";
        if (version == null) version = 0L;
    }
}
