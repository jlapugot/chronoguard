package io.chronoguard.junit5;

import io.chronoguard.TimeController;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;
import java.time.Instant;

public class TimeWarpExtension implements BeforeEachCallback {
    
    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestMethod().ifPresent(method -> {
            TimeWarp annotation = method.getAnnotation(TimeWarp.class);
            if (annotation != null) {
                if (!annotation.freezeAt().isEmpty()) {
                    TimeController.freeze(Instant.parse(annotation.freezeAt()));
                }
                if (!annotation.offsetBy().isEmpty()) {
                    TimeController.offset(Duration.parse(annotation.offsetBy()));
                }
            }
        });
    }
}
