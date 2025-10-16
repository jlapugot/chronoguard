package io.chronoguard.junit5;

import io.chronoguard.TimeController;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ChronoGuardExtension implements BeforeEachCallback, AfterEachCallback {
    
    @Override
    public void beforeEach(ExtensionContext context) {
        TimeController.reset();
    }
    
    @Override
    public void afterEach(ExtensionContext context) {
        TimeController.reset();
    }
}
