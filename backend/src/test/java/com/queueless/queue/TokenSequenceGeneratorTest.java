package com.queueless.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TokenSequenceGeneratorTest {

    private JdbcTemplate jdbcTemplate;
    private TokenSequenceGenerator sequenceGenerator;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        sequenceGenerator = new TokenSequenceGenerator(jdbcTemplate);
    }

    @Test
    @DisplayName("Format token number zero-pads 3 digits with service name prefix")
    public void testFormatTokenNumber() {
        assertEquals("EME-001", sequenceGenerator.formatTokenNumber("Emergency Triage", 1));
        assertEquals("EME-042", sequenceGenerator.formatTokenNumber("Emergency Triage", 42));
        assertEquals("CAR-100", sequenceGenerator.formatTokenNumber("Cardiology", 100));
        assertEquals("DEP-1000", sequenceGenerator.formatTokenNumber("Deposit Desk", 1000));
    }

    @Test
    @DisplayName("Initial token sequence returns 1 when no prior sequence exists for date")
    public void testInitialSequenceReturnsOne() {
        LocalDate today = LocalDate.of(2026, 10, 15);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1L), eq(today)))
                .thenReturn(Collections.emptyList());

        int seq = sequenceGenerator.nextSequenceNumber(1L, today);
        assertEquals(1, seq);
        verify(jdbcTemplate, times(1)).update(contains("INSERT INTO token_sequences"), eq(1L), eq(today));
    }

    @Test
    @DisplayName("Subsequent token sequence increments monotonically")
    public void testExistingSequenceIncrements() {
        LocalDate today = LocalDate.of(2026, 10, 15);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1L), eq(today)))
                .thenReturn(List.of(4));

        int nextSeq = sequenceGenerator.nextSequenceNumber(1L, today);
        assertEquals(5, nextSeq);
        verify(jdbcTemplate, times(1)).update(contains("UPDATE token_sequences"), eq(5), eq(1L), eq(today));
    }
}
