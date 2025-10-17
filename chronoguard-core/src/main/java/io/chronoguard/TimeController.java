package io.chronoguard;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public class TimeController {
    private static volatile Long frozenTimeMillis = null;
    private static volatile Long offsetMillis = 0L;
    
    public static void freeze(Instant instant) {
        frozenTimeMillis = instant.toEpochMilli();
    }
    
    public static void freeze(ZonedDateTime dateTime) {
        frozenTimeMillis = dateTime.toInstant().toEpochMilli();
    }
    
    public static void unfreeze() {
        frozenTimeMillis = null;
    }
    
    public static void offset(Duration duration) {
        offsetMillis = duration.toMillis();
    }
    
    public static void travel(Duration duration) {
        if (frozenTimeMillis != null) {
            frozenTimeMillis += duration.toMillis();
        } else {
            offsetMillis += duration.toMillis();
        }
    }
    
    public static void reset() {
        frozenTimeMillis = null;
        offsetMillis = 0L;
    }
    
    public static long getCurrentTimeMillis() {
        if (frozenTimeMillis != null) {
            return frozenTimeMillis + offsetMillis;
        }
        return System.currentTimeMillis() + offsetMillis;
    }
    
    public static Instant getCurrentInstant() {
        return Instant.ofEpochMilli(getCurrentTimeMillis());
    }
}
