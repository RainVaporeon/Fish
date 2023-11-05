package com.spiritlight.chess.fish;

import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.chess.fish.internal.exceptions.SystemError;
import com.spiritlight.chess.fish.internal.utils.boot.LaunchArgs;
import com.spiritlight.chess.fish.internal.utils.boot.LaunchOption;
import com.spiritlight.fishutils.collections.Pair;

import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) {
        registerLaunchOptions(InternLogger.class, () -> {
            Consumer<String> out = s -> InternLogger.setEnabled(Boolean.parseBoolean(s));
            return Pair.of("intern.logger", out);
        });
        registerLaunchOptions(SystemError.class, () -> Pair.of("system.error", s -> {}));

        try {
            LaunchArgs.init();
        } catch (Exception e) {
            InternLogger.setEnabled(true);
            InternLogger.getLogger().warn("The internal logger was turned on due to an internal error.");
            InternLogger.getLogger().fatal("Failed to initialize launch args: ", e);
            InternLogger.getLogger().error("The system will now halt.");
            System.exit(1);
            return;
        }
    }

    // The target is unused here, but indicates the source class this option
    // intends to target to.
    private static void registerLaunchOptions(Class<?> target, LaunchOption option) {
        LaunchArgs.register(option);
    }
}