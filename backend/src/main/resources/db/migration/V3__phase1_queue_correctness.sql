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

-- 3. Populate sequence_date for existing tokens
UPDATE tokens SET sequence_date = CURRENT_DATE WHERE sequence_date IS NULL;

-- 4. Enforce NOT NULL on sequence_date
ALTER TABLE tokens MODIFY COLUMN sequence_date DATE NOT NULL;

-- 5. Allow guest tokens by making user_id nullable
ALTER TABLE tokens MODIFY COLUMN user_id BIGINT NULL;

-- 6. Add indexes and uniqueness constraints
CREATE INDEX idx_token_guest_active ON tokens (guest_session_id, service_id, status);
CREATE UNIQUE INDEX uk_token_service_date_number ON tokens (service_id, sequence_date, token_number);
