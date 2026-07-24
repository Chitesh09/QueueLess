package com.queueless.domain.repository;

import com.queueless.domain.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByBranchId(Long branchId);
    List<ServiceEntity> findByOrganizationId(Long organizationId);
}
