package com.queueless.domain.repository;

import com.queueless.domain.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByBranchId(Long branchId);
    List<ServiceEntity> findByOrganizationId(Long organizationId);
    Optional<ServiceEntity> findByIdAndOrganizationId(Long id, Long organizationId);
}

