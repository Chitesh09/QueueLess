package com.queueless.domain.repository;

import com.queueless.domain.entity.Token;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByServiceIdAndStatusOrderByCreatedAtAsc(Long serviceId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Token t WHERE t.serviceId IN :serviceIds AND t.status = 'WAITING'")
    List<Token> findWaitingTokensForUpdate(@Param("serviceIds") List<Long> serviceIds);

    boolean existsByUserIdAndServiceIdAndStatusIn(Long userId, Long serviceId, List<String> activeStatuses);

    long countByServiceIdAndStatus(Long serviceId, String status);

    List<Token> findByStatusAndExpiresAtBefore(String status, Instant now);

    List<Token> findByBranchIdAndCreatedAtAfter(Long branchId, Instant since);
}
