package io.github.rainvaporeon.chess.fish.internal.exceptions;

import io.github.rainvaporeon.chess.fish.internal.InternLogger;
import io.github.rainvaporeon.chess.fish.internal.utils.boot.LaunchArgs;

/**
 * Exception denoting there may have been an irrecoverable issue with
 * the current engine. This should not be treated lightly as it may indicate
 * some sort of internal issues occurring in the code.
 *
 * @apiNote Despite naming convention indicates that this is an instance of
 * {@link Error}, at some cases the state may be recoverable and thus should
 * be handled accordingly. If the case is not expected, then the program should
 * be executed with the {@code -Dsystem.error} parameter.
 */
public class SystemError extends RuntimeException {
    public SystemError() { handle(); }
    public SystemError(String s) { super(s); handle(); }
    public SystemError(Throwable cause) { super(cause); handle(); }
    public SystemError(String s, Throwable cause) { super(s, cause); handle(); }

    private void handle() {
        if(LaunchArgs.isKeyPresent("system.error")) throw new Error(this);
        InternLogger.getLogger().fatal("System error caught: ", this);
    }
}
