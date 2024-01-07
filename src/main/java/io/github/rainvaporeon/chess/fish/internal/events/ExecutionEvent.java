package io.github.rainvaporeon.chess.fish.internal.events;

import com.spiritlight.fishutils.utils.eventbus.events.Event;

import java.util.UUID;

public class ExecutionEvent extends Event {
    private final Runnable runnable;
    private final UUID key;

    public ExecutionEvent(Runnable runnable, UUID key) {
        this.runnable = runnable;
        this.key = key;
    }

    public Runnable getRunnable(UUID key) {
        if(!this.key.equals(key)) return null;
        return runnable;
    }
}
