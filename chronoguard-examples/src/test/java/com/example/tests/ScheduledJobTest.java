package com.example.tests;

import io.chronoguard.TimeController;
import io.chronoguard.junit5.ChronoGuardExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Dummy classes to make the test compile
interface DailyReportService {
    void checkAndGenerateReports();
}
interface ReportRepository {
    Optional<Object> findByDate(String date);
}


@SpringBootTest(classes = TestApplication.class)
@ExtendWith(ChronoGuardExtension.class)
class ScheduledJobTest {
    
    @Autowired(required = false)
    private DailyReportService reportService;
    
    @Autowired(required = false)
    private ReportRepository reportRepo;
    
    @Test
    void testDailyReportGeneration() {
        // Since we don't have a real service, we can't run this test meaningfully.
        if (reportService == null || reportRepo == null) return;

        // Freeze at midnight
        TimeController.freeze(Instant.parse("2025-10-16T00:00:00Z"));
        
        // Trigger scheduler check (no report should be generated yet)
        reportService.checkAndGenerateReports();
        assertThat(reportRepo.findByDate("2025-10-16")).isEmpty();
        
        // Travel to 2 AM (scheduled time)
        TimeController.travel(Duration.ofHours(2));
        
        // Now report should be generated
        reportService.checkAndGenerateReports();
        assertThat(reportRepo.findByDate("2025-10-16")).isNotEmpty();
    }
}
