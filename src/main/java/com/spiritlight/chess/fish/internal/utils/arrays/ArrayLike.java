package com.spiritlight.chess.fish.internal.utils.arrays;

import com.spiritlight.fishutils.misc.annotations.New;

import java.io.Serializable;
import java.util.Iterator;

public abstract class ArrayLike<T> implements Cloneable, Iterable<T>, Serializable {

    public abstract T get(int index);

    public abstract void set(int index, T value);

    public abstract int size();

    public abstract boolean isMutable();

    public void fill(int from, int to, T value) {
        if(!isMutable()) throw new UnsupportedOperationException();
        for(int i = from; i < to; i++) {
            set(i, value);
        }
    }

    public @New T[] toArray() {
        Object[] ret = new Object[this.size()];
        for (int i = 0; i < size(); i++) {
            ret[i] = get(i);
        }
        return (T[]) ret;
    }

    @Override
    public @New Iterator<T> iterator() {
        return new ArrayIterator<>(this);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException error) {
            // Java's fault
            throw new InternalError(error);
        }
    }
}
