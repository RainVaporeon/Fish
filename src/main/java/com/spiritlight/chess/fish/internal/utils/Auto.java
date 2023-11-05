package com.spiritlight.chess.fish.internal.utils;

public class Auto {
    private final Object object;

    private Auto(Object o) {
        this.object = o;
    }

    public <T> T get() {
        return (T) object;
    }

    public static Auto create(Object o) {
        return new Auto(o);
    }
}
