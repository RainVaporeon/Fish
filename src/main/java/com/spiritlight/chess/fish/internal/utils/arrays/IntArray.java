package com.spiritlight.chess.fish.internal.utils.arrays;

import java.util.function.IntUnaryOperator;
import java.util.stream.StreamSupport;

/**
 * A class representing an optionally mutable integer array.
 * <p>
 *     This class provides multiple utility fields to be used
 *     in pair with {@link com.spiritlight.chess.fish.internal.utils.Array}.
 * </p>
 */
public class IntArray extends ArrayLike<Integer> {
    private final int[] array;
    private final boolean mutable;

    public IntArray() {
        this.array = new int[0];
        this.mutable = false;
    }

    public IntArray(int[] array) {
        this.array = array;
        this.mutable = false;
    }

    public IntArray(int[] array, boolean mutable) {
        this.array = array;
        this.mutable = mutable;
    }

    @Override
    public Integer get(int index) {
        checkRange(index);
        return array[index];
    }

    public int getAsInt(int index) {
        checkRange(index);
        return array[index];
    }

    @Override
    public void set(int index, Integer value) {
        setInt(index, value);
    }

    public void setInt(int index, int value) {
        if(mutable) {
            checkRange(index);
            array[index] = value;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public int[] toIntArray() {
        return StreamSupport.stream(this.spliterator(), false).mapToInt(Integer::intValue).toArray();
    }

    protected void checkRange(int val) {
        if(val < 0 || val >= size()) throw new IndexOutOfBoundsException(val);
    }

    public IntArray toMutable() {
        if(this.mutable) return this;
        return new IntArray(this.array.clone(), true);
    }

    public IntArray toImmutable() {
        if(!this.mutable) return this;
        return new IntArray(this.array.clone(), false);
    }

    @Override
    public boolean isMutable() {
        return mutable;
    }

    @Override
    public int size() {
        return array.length;
    }

    /*
        Static collections
     */

    public static final IntUnaryOperator NON_NEGATIVE = i -> Math.max(0, i);

    public static final IntUnaryOperator REVERSE = i -> -i;

    public static IntArray fromArray(int... array) {
        return new IntArray(array);
    }

    public static IntArray createEmpty(int size) {
        return new DefaultIntArray(size, 0);
    }

    public static IntArray create(int size, int value) {
        return new DefaultIntArray(size, value);
    }

    public static IntArray create(int size, IntUnaryOperator mapper) {
        int[] v = new int[size];
        for(int i = 0; i < size; i++) {
            v[i] = mapper.applyAsInt(i);
        }
        return new IntArray(v);
    }

    private static class DefaultIntArray extends IntArray {
        private final int size;
        private final int value;

        private DefaultIntArray(int size, int value) {
            super(null, false);
            if(size < 0) throw new IllegalArgumentException("size cannot be negative");
            this.size = size;
            this.value = value;
        }

        @Override
        public Integer get(int index) {
            checkRange(index);
            return value;
        }

        @Override
        public int getAsInt(int index) {
            checkRange(index);
            return value;
        }

        @Override
        protected void checkRange(int val) {
            if(val < 0 || val >= size) throw new IndexOutOfBoundsException(val);
        }
    }
}
