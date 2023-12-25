package com.spiritlight.chess.fish.test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

public class Tests {
    private final Queue<TestComponent> components = new ArrayDeque<>();

    private boolean printTimeData;

    public void add(TestComponent component) {
        this.components.add(component);
    }

    public void run() {
        components.forEach(cmp -> {
            long l = System.nanoTime(), f;
            try {
                cmp.test();
                f = System.nanoTime();
                System.out.println(STR."Test case finished for \{cmp.getIdentifier()}: Time taken: \{f - l}ns");
            } catch (Throwable t) {
                f = System.nanoTime();
                System.out.println(STR."Test case failed for \{cmp.getIdentifier()}: Time taken: \{f - l}ns");
            }
        });
    }
}
