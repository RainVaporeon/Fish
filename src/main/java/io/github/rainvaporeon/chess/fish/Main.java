package io.github.rainvaporeon.chess.fish;

import io.github.rainvaporeon.chess.fish.game.utils.board.Magic;
import io.github.rainvaporeon.chess.fish.internal.InternLogger;
import io.github.rainvaporeon.chess.fish.internal.exceptions.SystemError;
import io.github.rainvaporeon.chess.fish.internal.utils.boot.LaunchArgs;
import io.github.rainvaporeon.chess.fish.internal.utils.boot.LaunchOption;
import com.spiritlight.fishutils.collections.Pair;
import com.spiritlight.fishutils.logging.Logger;
import com.spiritlight.fishutils.logging.Loggers;

import java.util.function.Consumer;

public class Main {
    private static final Logger log = Loggers.getLogger("Fish/Launch");

    public static void main(String[] args) {
        registerLaunchOptions(InternLogger.class, () -> {
            Consumer<String> out = s -> InternLogger.setEnabled(Boolean.parseBoolean(s));
            return Pair.of("intern.logger", out);
        });
        registerLaunchOptions(SystemError.class, () -> Pair.of("system.error", _ -> {}));

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
        log.info(STR."Defined LaunchOption \{System.identityHashCode(option)} as a launch property of \{target}");
        LaunchArgs.register(option);
    }
}