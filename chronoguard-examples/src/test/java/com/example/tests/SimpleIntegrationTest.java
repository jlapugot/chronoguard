package com.example.tests;

import io.chronoguard.TimeController;
import io.chronoguard.junit5.ChronoGuardExtension;
import io.chronoguard.junit5.TimeWarp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple integration test to verify ChronoGuard works with published artifacts.
 * This test uses the published version from Maven Central.
 */
@ExtendWith(ChronoGuardExtension.class)
public class SimpleIntegrationTest {

    @Test
    @TimeWarp(freezeAt = "2024-01-15T10:30:00Z")
    void testTimeWarpAnnotation() {
        // Time should be frozen at the specified instant
        Instant now = Instant.now();
        assertThat(now).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));

        long millis = System.currentTimeMillis();
        assertThat(millis).isEqualTo(Instant.parse("2024-01-15T10:30:00Z").toEpochMilli());
    }

    @Test
    void testProgrammaticTimeControl() {
        // Freeze time using TimeController
        Instant frozen = Instant.parse("2024-12-25T00:00:00Z");
        TimeController.freeze(frozen);

        assertThat(Instant.now()).isEqualTo(frozen);
        assertThat(System.currentTimeMillis()).isEqualTo(frozen.toEpochMilli());
    }

    @Test
    void testTimeTravel() {
        // Start at a specific time
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        TimeController.freeze(start);

        assertThat(Instant.now()).isEqualTo(start);

        // Travel 7 days forward
        TimeController.travel(Duration.ofDays(7));

        Instant expected = start.plus(Duration.ofDays(7));
        assertThat(Instant.now()).isEqualTo(expected);
    }

    @Test
    void testLocalDateTimeInterception() {
        // Freeze time
        Instant frozen = Instant.parse("2024-06-15T14:30:00Z");
        TimeController.freeze(frozen);

        // LocalDateTime.now() should also be intercepted
        LocalDateTime ldt = LocalDateTime.now();
        assertThat(ldt.getYear()).isEqualTo(2024);
        assertThat(ldt.getMonthValue()).isEqualTo(6);
        assertThat(ldt.getDayOfMonth()).isEqualTo(15);
    }
}
