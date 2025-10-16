package com.example.tests;

import io.chronoguard.TimeController;
import io.chronoguard.junit5.ChronoGuardExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Dummy classes to make the test compile
class User {
    String name;
    User(String name) { this.name = name; }
}

class UserCache {
    private final Map<String, User> cache = new HashMap<>();
    private final Map<String, Long> timestamps = new HashMap<>();
    private static final long TTL = Duration.ofMinutes(5).toMillis();

    public User get(String key) {
        if (timestamps.containsKey(key) && (TimeController.getCurrentTimeMillis() - timestamps.get(key)) > TTL) {
            cache.remove(key);
            timestamps.remove(key);
            return null;
        }
        return cache.get(key);
    }

    public void put(String key, User user) {
        cache.put(key, user);
        timestamps.put(key, TimeController.getCurrentTimeMillis());
    }
}


@ExtendWith(ChronoGuardExtension.class)
class CacheTest {
    
    private final UserCache cache = new UserCache();
    
    @Test
    void testCacheExpiration() {
        cache.put("user1", new User("Alice"));
        assertThat(cache.get("user1")).isNotNull();
        
        // Travel 4 minutes forward (cache TTL is 5 minutes)
        TimeController.travel(Duration.ofMinutes(4));
        assertThat(cache.get("user1")).isNotNull();
        
        // Travel 2 more minutes (total 6 minutes)
        TimeController.travel(Duration.ofMinutes(2));
        assertThat(cache.get("user1")).isNull(); // Expired!
    }
}
