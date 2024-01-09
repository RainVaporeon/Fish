package io.github.rainvaporeon.chess.fish.internal.collections;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class PriorityHolder<T> extends AbstractQueue<T> {
    private final T[] array;
    private final Comparator<T> comparator;

    private int size;

    @SuppressWarnings("unchecked")
    public PriorityHolder(int size, Comparator<T> comparator) {
        this.array = (T[]) new Object[size];
        this.comparator = comparator;
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(T t) {
        if(array.length < size - 1 && comparator.compare(t, array[size - 1]) < 0) {
            array[++size] = t;
            Arrays.sort(array, comparator);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public T poll() {
        if(size > 0) {
            T t = array[0];
            array[0] = null;
            Arrays.sort(array, comparator);
            return t;
        } else {
            return null;
        }
    }

    @Override
    public T peek() {
        if(size == 0) return null;
        return array[0];
    }

    private class Itr implements Iterator<T> {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public T next() {
            return array[cursor++];
        }
    }
}
