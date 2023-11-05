package com.spiritlight.chess.fish.internal.utils.arrays;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
    private final T[] shadow;
    private int cursor;

    public ArrayIterator(ArrayLike<T> shadow) {
        this(shadow.toArray());
    }

    public ArrayIterator(T[] shadow) {
        this.shadow = shadow;
    }

    @Override
    public boolean hasNext() {
        return cursor < shadow.length;
    }

    @Override
    public T next() {
        return shadow[cursor++];
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
