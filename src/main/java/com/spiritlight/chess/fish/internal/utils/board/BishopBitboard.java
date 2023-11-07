package com.spiritlight.chess.fish.internal.utils.board;

import com.spiritlight.fishutils.misc.arrays.primitive.IntArray;

public class BishopBitboard extends GameBitboard {
    // Ideally, stay in the diagonals and get away from the center
    private static final IntArray early = IntArray.fromArray(
            64, 32, 16, 8, 8, 16, 32, 64,
            32, 64, 32, 16, 16, 32, 64, 32,
            16, 32, 64, 32, 32, 64, 32, 16,
             8, 16, 16,  0,  0, 16, 16,  8,
             8, 16, 16,  0,  0, 16, 16,  8,
            16, 32, 64, 32, 32, 64, 32, 16,
            32, 64, 32, 16, 16, 32, 64, 32,
            64, 32, 16, 8, 8, 16, 32, 64
    );

    private static final IntArray middle = IntArray.fromArray(
            -29,   4, -82, -37, -25, -42,   7,  -8,
            -26,  16, -18, -13,  30,  59,  18, -47,
            -16,  37,  43,  40,  35,  50,  37,  -2,
            -4,   5,  19,  50,  37,  37,   7,  -2,
            -6,  13,  13,  26,  34,  12,  10,   4,
            0,  15,  15,  15,  14,  27,  18,  10,
            4,  15,  16,   0,   7,  21,  33,   1,
            -33,  -3, -14, -21, -13, -12, -39, -21
    );

    // More extreme in the end game, also that bishops get a multiplier
    // as they are generally more useful in the endgame.
    private static final IntArray end = IntArray.fromArray(
            -14, -21, -11,  -8, -7,  -9, -17, -24,
            -8,  -4,   7, -12, -3, -13,  -4, -14,
            2,  -8,   0,  -1, -2,   6,   0,   4,
            -3,   9,  12,   9, 14,  10,   3,   2,
            -6,   3,  13,  19,  7,  10,  -3,  -9,
            -12,  -3,   8,  10, 13,   3,  -7, -15,
            -14, -18,  -7,  -1,  4,  -9, -15, -27,
            -23,  -9, -23,  -5, -9, -16,  -5, -17
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
