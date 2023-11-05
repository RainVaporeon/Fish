package com.spiritlight.chess.fish.internal;

import com.spiritlight.fishutils.logging.ILogger;
import com.spiritlight.fishutils.logging.Loggers;

public class InternLogger {
    private static boolean enabled;

    public static void setEnabled(boolean enabled) {
        InternLogger.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static ILogger getLogger() {
        if(!enabled) return Loggers.noop();

        return Loggers.getLogger("Fish/Intern");
    }
}
