package com.spiritlight.chess.fish.internal.utils;

import com.spiritlight.fishutils.collections.IntList;

public class Bits {
    private static final double LOG_2 = Math.log(2);

    public static int[] bitList(long value) {
        IntList list = new IntList(8);
        while(value != 0) {
            long mask = Long.highestOneBit(value);
            int position = (int) (Math.log(mask) / LOG_2) - 1; // return actual position
            value ^= mask; // turn that bit off
            list.add(position); // record the position
        }
        return list.toIntArray();
    }
}
