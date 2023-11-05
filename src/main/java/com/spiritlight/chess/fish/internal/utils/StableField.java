package com.spiritlight.chess.fish.internal.utils;

public class StableField<T> {
    private T t;
    private boolean modified;

    public StableField(T t) {
        this.t = t;
        this.modified = false;
    }

    public void set(T t) {
        if(modified) throw new IllegalStateException("stable field modified more than once");
        this.modified = true;
        this.t = t;
    }

    public T get() {
        return t;
    }

    @Override
    public String toString() {
        return String.valueOf(t);
    }
}
