package com.spiritlight.chess.fish.internal.utils.board;

import com.spiritlight.chess.fish.internal.utils.arrays.IntArray;

public class KnightBitboard extends GameBitboard {
    /**
     * Discourages from staying in the rim
     */
    private static final IntArray early = IntArray.fromArray(
            -15, -15, -15, -15, -15, -15, -15, -15,
                   -15,   0,   0,   0,   0,   0,   0, -15,
                   -10,   0,  15,  15,  15,  15,   0, -10,
                   -15,  15,  25,  25,  25,  25,  15, -10,
                   -10,  15,  25,  25,  25,  25,  15, -10,
                   -10,   0,  15,  15,  15,  15,   0, -15,
                   -15,   0,   0,   0,   0,   0,   0, -15,
                   -15, -15, -15, -15, -15, -15, -15, -15
    );

    /**
     * Middle game, further encourages gaining scope in the middle and
     * further discouraging against the rim
     */
    private static final IntArray middle = IntArray.fromArray(
            -167, -89, -34, -49,  61, -97, -15, -107,
            -73, -41,  72,  36,  23,  62,   7,  -17,
            -47,  60,  37,  65,  84, 129,  73,   44,
            -9,  17,  19,  53,  37,  69,  18,   22,
            -13,   4,  16,  13,  28,  19,  21,   -8,
            -23,  -9,  12,  10,  19,  17,  25,  -16,
            -29, -53, -12,  -3,  -1,  18, -14,  -19,
            -105, -21, -58, -33, -17, -28, -19,  -23
    );

    /**
     * Endgame, about nothing matters, but still encourages staying
     * in the middle for more squares covered.
     */
    private static final IntArray end = IntArray.fromArray(
            -58, -38, -13, -28, -31, -27, -63, -99,
            -25,  -8, -25,  -2,  -9, -25, -24, -52,
            -24, -20,  10,   9,  -1,  -9, -19, -41,
            -17,   3,  22,  22,  22,  11,   8, -18,
            -18,  -6,  16,  25,  16,  17,   4, -18,
            -23,  -3,  -1,  15,  10,  -3, -20, -22,
            -42, -20, -10,  -5,  -2, -20, -23, -44,
            -29, -51, -23, -15, -22, -18, -50, -64
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
