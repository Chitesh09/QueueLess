package com.queueless.domain.repository;

import com.queueless.domain.entity.TokenSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TokenSequenceRepository extends JpaRepository<TokenSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TokenSequence s WHERE s.serviceId = :serviceId AND s.sequenceDate = :date")
    Optional<TokenSequence> findByServiceIdAndSequenceDateForUpdate(@Param("serviceId") Long serviceId,
                                                                   @Param("date") LocalDate date);

    Optional<TokenSequence> findByServiceIdAndSequenceDate(Long serviceId, LocalDate date);
}
