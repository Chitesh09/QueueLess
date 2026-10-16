package com.queueless.domain.repository;

import com.queueless.domain.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {
    List<Counter> findByBranchId(Long branchId);
    List<Counter> findByBranchIdAndOrganizationId(Long branchId, Long organizationId);
    Optional<Counter> findByIdAndOrganizationId(Long id, Long organizationId);
}

