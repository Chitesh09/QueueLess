package com.queueless.queue;

import com.queueless.domain.entity.Token;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrioritySchedulerTest {

    @Test
    public void testEmergencySelectedFirstOverStandard() {
        PriorityScheduler scheduler = new PriorityScheduler();
        ReflectionTestUtils.setField(scheduler, "maxStarvationThresholdSec", 600L);

        Instant now = Instant.now();
        Token emergency = Token.builder().id(1L).priorityClass("EMERGENCY").createdAt(now.minusSeconds(10)).build();
        Token standard = Token.builder().id(2L).priorityClass("STANDARD").createdAt(now.minusSeconds(30)).build();

        Token selected = scheduler.selectNextToken(List.of(standard, emergency));
        assertEquals(1L, selected.getId());
    }

    @Test
    public void testStarvationPreventionElevatesLongWaitingStandardToken() {
        PriorityScheduler scheduler = new PriorityScheduler();
        ReflectionTestUtils.setField(scheduler, "maxStarvationThresholdSec", 600L);

        Instant now = Instant.now();
        // Standard token waiting for 45 minutes (2700s) vs newly arrived emergency token (10s)
        Token emergencyNew = Token.builder().id(1L).priorityClass("EMERGENCY").createdAt(now.minusSeconds(10)).build();
        Token standardStarved = Token.builder().id(2L).priorityClass("STANDARD").createdAt(now.minusSeconds(2700)).build();

        double emergencyScore = scheduler.calculatePriorityScore(emergencyNew, now);
        double starvedStandardScore = scheduler.calculatePriorityScore(standardStarved, now);

        // Standard token starved for 2700s should have a higher score than a fresh emergency token
        assertEquals(2L, scheduler.selectNextToken(List.of(emergencyNew, standardStarved)).getId());
    }
}
