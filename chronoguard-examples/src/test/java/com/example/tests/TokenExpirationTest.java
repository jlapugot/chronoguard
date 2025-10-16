package com.example.tests;

import io.chronoguard.TimeController;
import io.chronoguard.junit5.ChronoGuardExtension;
import io.chronoguard.junit5.TimeWarp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

// Dummy classes to make the test compile
interface TokenService {
    String createToken(String userId);
    boolean isValid(String token);
    Instant getExpiration(String token);
}

@SpringBootTest(classes = TestApplication.class) 
@ExtendWith(ChronoGuardExtension.class)
class TokenExpirationTest {
    
    // This would be a real service in a real app
    @Autowired(required = false)
    private TokenService tokenService;
    
    @Test
    void testTokenExpiresAfter1Hour() {
        // Since we don't have a real service, we can't run this test meaningfully.
        // This is just for compilation.
        if (tokenService == null) return;

        // Create token (valid for 1 hour)
        String token = tokenService.createToken("user123");
        assertThat(tokenService.isValid(token)).isTrue();
        
        // Travel 30 minutes forward
        TimeController.travel(Duration.ofMinutes(30));
        assertThat(tokenService.isValid(token)).isTrue();
        
        // Travel another 31 minutes (total 61 minutes)
        TimeController.travel(Duration.ofMinutes(31));
        assertThat(tokenService.isValid(token)).isFalse();
    }
    
    @Test 
    @TimeWarp(freezeAt = "2025-01-15T10:00:00Z")
    void testAtSpecificTime() {
        // Since we don't have a real service, we can't run this test meaningfully.
        if (tokenService == null) return;

        // Time is frozen at 2025-01-15 10:00 AM UTC
        String token = tokenService.createToken("user456");
        
        // Verify expiration is exactly 1 hour later
        Instant expiration = tokenService.getExpiration(token);
        assertThat(expiration).isEqualTo(Instant.parse("2025-01-15T11:00:00Z"));
    }
}
