package com.queueless.domain.repository;

import com.queueless.domain.entity.CounterServiceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterServiceMappingRepository extends JpaRepository<CounterServiceMapping, CounterServiceMapping.CounterServiceId> {
    List<CounterServiceMapping> findByCounterId(Long counterId);
    List<CounterServiceMapping> findByServiceId(Long serviceId);
}
