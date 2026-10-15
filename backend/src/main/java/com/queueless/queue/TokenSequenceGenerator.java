package com.queueless.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class TokenSequenceGenerator {

    private static final Logger log = LoggerFactory.getLogger(TokenSequenceGenerator.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate requiresNewTxTemplate;

    public TokenSequenceGenerator(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.requiresNewTxTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTxTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Atomically allocates the next persistent sequence number for a given service and date.
     * Uses pessimistic row locking (FOR UPDATE) inside the main transaction.
     * When sequence row does not exist yet (first token of the day), row initialization is safely
     * performed via an isolated REQUIRES_NEW transaction so that any concurrent collision
     * never touches, taints, or aborts the caller's main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int nextSequenceNumber(Long serviceId, LocalDate date) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
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

            // Sequence row does not exist yet: safely initialize in an isolated transaction
            ensureSequenceRowInitialized(serviceId, date);
        }

        throw new IllegalStateException("Failed to allocate token sequence after " + maxAttempts + " attempts");
    }

    /**
     * Ensures an initial sequence row exists (with last_number = 0) in an isolated transaction.
     * If a concurrent thread initializes it simultaneously, the unique constraint collision
     * is safely absorbed within this isolated transaction boundary, leaving the caller's transaction clean.
     */
    public void ensureSequenceRowInitialized(Long serviceId, LocalDate date) {
        requiresNewTxTemplate.executeWithoutResult(status -> {
            try {
                jdbcTemplate.update(
                        "INSERT INTO token_sequences (service_id, sequence_date, last_number, created_at, updated_at) VALUES (?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                        serviceId, date
                );
            } catch (DataIntegrityViolationException e) {
                log.debug("Concurrent sequence initialization collision for service {} on {} absorbed in isolated tx.", serviceId, date);
            }
        });
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
