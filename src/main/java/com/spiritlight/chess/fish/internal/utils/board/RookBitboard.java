package com.spiritlight.chess.fish.internal.utils.board;

import com.spiritlight.chess.fish.internal.utils.arrays.IntArray;

public class RookBitboard extends GameBitboard {
    private static final IntArray early = IntArray.createEmpty(64);

    private static final IntArray middle = IntArray.fromArray(
        32,  42,  32,  51, 63,  9,  31,  43,
                27,  32,  58,  62, 80, 67,  26,  44,
                -5,  19,  26,  36, 17, 45,  61,  16,
                -24, -11,   7,  26, 24, 35,  -8, -20,
                -36, -26, -12,  -1,  9, -7,   6, -23,
                -45, -25, -16, -17,  3,  0,  -5, -33,
                -44, -16, -20,  -9, -1, 11,  -6, -71,
                -19, -13,   1,  17, 16,  7, -37, -26
    );

    private static final IntArray end = IntArray.fromArray(
            13, 10, 18, 15, 12,  12,   8,   5,
            11, 13, 13, 11, -3,   3,   8,   3,
            7,  7,  7,  5,  4,  -3,  -5,  -3,
            4,  3, 13,  1,  2,   1,  -1,   2,
            3,  5,  8,  4, -5,  -6,  -8, -11,
            -4,  0, -5, -1, -7, -12,  -8, -16,
            -6, -6,  0,  2, -9,  -9, -11,  -3,
            -9,  2,  3, -1, -5, -13,   4, -20
    );

    @Override
    protected IntArray early() {
        return early;
    }

    @Override
    protected IntArray middle() {
        return middle;
    }

    @Override
    protected IntArray end() {
        return end;
    }
}
