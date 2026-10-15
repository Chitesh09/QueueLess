-- QueueLess Phase 1: Queue Core Correctness Migration
-- Version: V3__phase1_queue_correctness.sql

-- 1. Create persistent token sequences table
CREATE TABLE IF NOT EXISTS token_sequences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id BIGINT NOT NULL,
    sequence_date DATE NOT NULL,
    last_number INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_seq_service_date UNIQUE (service_id, sequence_date),
    CONSTRAINT fk_seq_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Add sequence_date and guest_session_id to tokens table
ALTER TABLE tokens ADD COLUMN sequence_date DATE NULL;
ALTER TABLE tokens ADD COLUMN guest_session_id VARCHAR(100) NULL;

-- 3. Populate sequence_date for existing tokens from created_at
UPDATE tokens SET sequence_date = COALESCE(DATE(created_at), CURRENT_DATE) WHERE sequence_date IS NULL;

-- 4. Deduplicate any historical duplicate token numbers before enforcing unique constraint
UPDATE tokens 
SET token_number = CONCAT(token_number, '-HIST-', id)
WHERE id IN (
    SELECT dup.id FROM (
        SELECT t1.id 
        FROM tokens t1
        INNER JOIN tokens t2 ON t1.service_id = t2.service_id 
                            AND t1.sequence_date = t2.sequence_date 
                            AND t1.token_number = t2.token_number 
                            AND t1.id > t2.id
    ) dup
);

-- 5. Enforce NOT NULL on sequence_date
ALTER TABLE tokens MODIFY COLUMN sequence_date DATE NOT NULL;

-- 6. Allow guest tokens by making user_id nullable
ALTER TABLE tokens MODIFY COLUMN user_id BIGINT NULL;

-- 7. Add indexes and uniqueness constraints
CREATE INDEX idx_token_guest_active ON tokens (guest_session_id, service_id, status);
CREATE UNIQUE INDEX uk_token_service_date_number ON tokens (service_id, sequence_date, token_number);

-- 8. Backfill token_sequences starting at highest previously allocated number (or count if higher)
INSERT INTO token_sequences (service_id, sequence_date, last_number, created_at, updated_at)
SELECT 
    service_id, 
    sequence_date, 
    GREATEST(
        COUNT(*),
        COALESCE(MAX(
            CASE 
                WHEN token_number REGEXP '[0-9]+$' THEN CAST(REGEXP_REPLACE(token_number, '^.*-', '') AS SIGNED)
                ELSE 0 
            END
        ), 0)
    ) AS last_number,
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
FROM tokens
GROUP BY service_id, sequence_date;
