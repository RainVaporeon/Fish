package com.spiritlight.chess.fish.internal.utils.board;

import com.spiritlight.chess.fish.internal.utils.arrays.IntArray;

public class PawnBitboard extends GameBitboard {

    /**
     * A general board somewhat encouraging pawn pushes
     */
    private static final IntArray early = IntArray.fromArray(
            0, 0, 0, 0, 0, 0, 0, 0,
            65, 80, 80, 85, 85, 80, 80, 65,
            55, 65, 65, 70, 70, 65, 65, 55,
            45, 55, 55, 60, 60, 55, 55, 45,
            35, 45, 45, 50, 50, 45, 45, 35,
            25, 35, 35, 40, 40, 35, 35, 25,
            15, 25, 25, 30, 30, 25, 25, 15,
            0, 0, 0, 0, 0, 0, 0, 0);

    /**
     * Still encouraging gaining space from pushing pawns
     */
    private static final IntArray middle = IntArray.fromArray(
            0,   0,   0,   0,   0,   0,  0,   0,
            98, 134,  61,  95,  68, 126, 34, -11,
            -6,   7,  26,  31,  65,  56, 25, -20,
            -14,  13,   6,  21,  23,  12, 17, -23,
            -27,  -2,  -5,  12,  17,   6, 10, -25,
            -26,  -4,  -4, -10,   3,   3, 33, -12,
            -35,  -1, -20, -23, -15,  24, 38, -22,
            0,   0,   0,   0,   0,   0,  0,   0
            );
    /**
     * Endgame, priority readjusted slightly.
     */
    private static final IntArray end = IntArray.fromArray(
            0,   0,   0,   0,   0,   0,   0,   0,
            178, 173, 158, 134, 147, 132, 165, 187,
            94, 100,  85,  67,  56,  53,  82,  84,
            32,  24,  13,   5,  -2,   4,  17,  17,
            13,   9,  -3,  -7,  -7,  -8,   3,  -1,
            4,   7,  -6,   1,   0,  -5,  -1,  -8,
            13,   8,   8,  10,  13,   0,   2,  -7,
            0,   0,   0,   0,   0,   0,   0,   0
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
