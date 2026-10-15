package com.queueless.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TokenSequenceGenerator {

    private static final Logger log = LoggerFactory.getLogger(TokenSequenceGenerator.class);

    private final JdbcTemplate jdbcTemplate;

    public TokenSequenceGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Atomically allocates the next persistent sequence number for a given service and date.
     * Uses database row locking (FOR UPDATE) via JdbcTemplate to avoid Hibernate persistence context
     * session taint under concurrent initialization.
     * Runs in REQUIRES_NEW to ensure the sequence reservation commits cleanly and releases locks immediately.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int nextSequenceNumber(Long serviceId, LocalDate date) {
        List<Integer> list = jdbcTemplate.query(
                "SELECT last_number FROM token_sequences WHERE service_id = ? AND sequence_date = ? FOR UPDATE",
                (rs, rowNum) -> rs.getInt("last_number"),
                serviceId, date
        );

        if (!list.isEmpty()) {
            int nextVal = list.get(0) + 1;
            jdbcTemplate.update(
                    "UPDATE token_sequences SET last_number = ?, updated_at = CURRENT_TIMESTAMP WHERE service_id = ? AND sequence_date = ?",
                    nextVal, serviceId, date
            );
            return nextVal;
        }

        // First token of the day for this service: attempt to insert initial record with lastNumber = 1
        try {
            jdbcTemplate.update(
                    "INSERT INTO token_sequences (service_id, sequence_date, last_number, created_at, updated_at) VALUES (?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    serviceId, date
            );
            return 1;
        } catch (DataIntegrityViolationException e) {
            log.info("Concurrent sequence initialization collision for service {} on {}. Retrying with lock.", serviceId, date);
            Integer current = jdbcTemplate.queryForObject(
                    "SELECT last_number FROM token_sequences WHERE service_id = ? AND sequence_date = ? FOR UPDATE",
                    Integer.class,
                    serviceId, date
            );
            int nextVal = (current != null ? current : 1) + 1;
            jdbcTemplate.update(
                    "UPDATE token_sequences SET last_number = ?, updated_at = CURRENT_TIMESTAMP WHERE service_id = ? AND sequence_date = ?",
                    nextVal, serviceId, date
            );
            return nextVal;
        }
    }

    /**
     * Formats the token number using service name prefix and 3-digit zero-padded number (e.g., EME-001).
     */
    public String formatTokenNumber(String serviceName, int sequenceNumber) {
        String cleanName = (serviceName != null && !serviceName.isBlank()) ? serviceName.trim() : "SVC";
        String prefix = cleanName.substring(0, Math.min(3, cleanName.length())).toUpperCase();
        return String.format("%s-%03d", prefix, sequenceNumber);
    }
}
