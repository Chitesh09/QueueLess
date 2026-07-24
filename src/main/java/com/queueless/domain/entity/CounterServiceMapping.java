package com.queueless.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "counter_service_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CounterServiceMapping.CounterServiceId.class)
public class CounterServiceMapping {

    @Id
    @Column(name = "counter_id")
    private Long counterId;

    @Id
    @Column(name = "service_id")
    private Long serviceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CounterServiceId implements Serializable {
        private Long counterId;
        private Long serviceId;
    }
}
