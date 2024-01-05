package io.github.rainvaporeon.chess.fish.test;

import com.spiritlight.fishutils.action.ActionResult;

public class TestComponent {
    private final String id;
    private final Runnable runnable;

    public TestComponent(String id, Runnable runnable) {
        this.id = id;
        this.runnable = runnable;
    }

    public String getIdentifier() {
        return id;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public ActionResult<Void> test() {
        return ActionResult.run(runnable::run);
    }

    public static TestComponent load(String id, Runnable runnable) {
        return new TestComponent(id, runnable);
    }
}
