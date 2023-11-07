package com.spiritlight.chess.fish.internal;

import com.spiritlight.fishutils.logging.ILogger;
import com.spiritlight.fishutils.logging.Logger;
import com.spiritlight.fishutils.logging.Loggers;

public class InternLogger {
    private static boolean enabled;
    private static final Logger LOGGER = Loggers.getLogger("Fish/Intern");

    public static void setEnabled(boolean enabled) {
        InternLogger.enabled = enabled;
        if(enabled) initializeLogger();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static ILogger getLogger() {
        if(!enabled) return Loggers.noop();

        return LOGGER;
    }

    private static void initializeLogger() {
        LOGGER.configured();
    }
}
