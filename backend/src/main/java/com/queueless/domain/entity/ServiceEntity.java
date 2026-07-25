package com.queueless.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private String name;

    @Column(name = "avg_duration_min", nullable = false)
    private Integer avgDurationMin;

    @Column(name = "daily_capacity", nullable = false)
    private Integer dailyCapacity;

    @Column(name = "grace_period_min", nullable = false)
    private Integer gracePeriodMin;

    @Column(name = "requires_identity_verification", nullable = false)
    private Boolean requiresIdentityVerification;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (avgDurationMin == null) avgDurationMin = 10;
        if (dailyCapacity == null) dailyCapacity = 200;
        if (gracePeriodMin == null) gracePeriodMin = 15;
        if (requiresIdentityVerification == null) requiresIdentityVerification = false;
    }
}
